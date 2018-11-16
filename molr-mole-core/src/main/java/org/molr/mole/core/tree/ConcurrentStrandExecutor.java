package org.molr.mole.core.tree;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.Result;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.tree.exception.RejectedCommandException;
import org.molr.mole.core.tree.exception.StrandExecutorException;
import org.molr.mole.core.utils.Trees;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;
import static org.molr.commons.domain.RunState.FINISHED;
import static org.molr.commons.domain.RunState.PAUSED;
import static org.molr.commons.domain.RunState.RUNNING;
import static org.molr.commons.domain.StrandCommand.PAUSE;
import static org.molr.commons.domain.StrandCommand.RESUME;
import static org.molr.commons.domain.StrandCommand.SKIP;
import static org.molr.commons.domain.StrandCommand.STEP_INTO;
import static org.molr.commons.domain.StrandCommand.STEP_OVER;
import static org.molr.mole.core.utils.Exceptions.exception;
import static org.molr.mole.core.utils.ThreadFactories.namedThreadFactory;

/**
 * Concurrent (non-blocking) implementation of a {@link StrandExecutor}. Internally all the operations run on a separate
 * thread avoiding to block the {@link #instruct(StrandCommand)} method (or any other for that matter).
 * <p>
 * This class is thread safe
 */
public class ConcurrentStrandExecutor implements StrandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentStrandExecutor.class);
    private static final int EXECUTOR_SLEEP_MS_IDLE = 50;
    private static final int EXECUTOR_SLEEP_MS_DEFAULT = 10;
    private static final int EXECUTOR_SLEEP_MS_WAITING_FOR_CHILDREN = 25;

    private final Object cycleLock = new Object();

    private final ExecutorService executor;
    private final LinkedBlockingQueue<StrandCommand> commandQueue;
    private final TreeStructure structure;
    private final Strand strand;
    private final StrandFactory strandFactory;
    private final StrandExecutorFactory strandExecutorFactory;
    private final LeafExecutor leafExecutor;

    private final ReplayProcessor<StrandCommand> lastCommandSink;
    private final Flux<StrandCommand> lastCommandStream;
    private final ReplayProcessor<RunState> stateSink;
    private final Flux<RunState> stateStream;
    private final ReplayProcessor<Block> blockSink;
    private final Flux<Block> blockStream;
    private final EmitterProcessor<Exception> errorSink;
    private final Flux<Exception> errorStream;
    private final EmitterProcessor<ImmutableList<StrandExecutor>> childExecutorsSink;

    /* AtomicReference guarantee read safety while not blocking using cycleLock for the getters */
    private final AtomicReference<Set<StrandCommand>> allowedCommands;
    private final AtomicReference<ExecutorState> actualState;
    private final AtomicReference<Block> actualBlock;

    private Block currentStepOverSource;
    private ImmutableList<StrandExecutor> childExecutors;

    public ConcurrentStrandExecutor(Strand strand, Block actualBlock, TreeStructure structure, StrandFactory strandFactory, StrandExecutorFactory strandExecutorFactory, LeafExecutor leafExecutor) {
        requireNonNull(actualBlock, "actualBlock cannot be null");
        this.structure = requireNonNull(structure, "structure cannot be null");
        this.strand = requireNonNull(strand, "strand cannot be null");
        this.strandFactory = requireNonNull(strandFactory, "strandFactory cannot be null");
        this.strandExecutorFactory = requireNonNull(strandExecutorFactory, "strandExecutorFactory cannot be null");
        this.leafExecutor = requireNonNull(leafExecutor, "leafExecutor cannot be null");

        this.lastCommandSink = ReplayProcessor.cacheLast();
        this.lastCommandStream = lastCommandSink.publishOn(publishingScheduler("last-command"));
        this.errorSink = EmitterProcessor.create();
        this.errorStream = errorSink.publishOn(publishingScheduler("errors"));
        this.stateSink = ReplayProcessor.cacheLast();
        this.stateStream = stateSink.publishOn(publishingScheduler("states"));
        this.blockSink = ReplayProcessor.cacheLast();
        this.blockStream = blockSink.publishOn(publishingScheduler("cursor"));
        this.childExecutorsSink = EmitterProcessor.create();

        this.allowedCommands = new AtomicReference<>();
        this.actualBlock = new AtomicReference<>();
        this.actualState = new AtomicReference<>();
        this.currentStepOverSource = null;

        updateActualBlock(actualBlock);
        updateState(ExecutorState.IDLE);
        updateChildrenExecutors(ImmutableList.of());

        this.commandQueue = new LinkedBlockingQueue<>(1);
        this.executor = Executors.newSingleThreadExecutor(namedThreadFactory("strand" + strand.id() + "-exec-%d"));
        this.executor.submit(this::lifecycle);
    }

    @Override
    public void instruct(StrandCommand command) {
        if (!commandQueue.offer(command)) {
            LOGGER.warn("Command {} cannot be accepted by strand {} because it is processing another command", command, strand);
        }
    }

    private void lifecycle() {
        // FIXME refactor in a more maintainable way, after tests are complete!
        boolean finished = false;
        while (!finished) {

            synchronized (cycleLock) {
                if (actualState() == ExecutorState.FINISHED) {
                    finished = true;
                    continue;
                }
                /* remove finished children */
                if (hasChildren() && actualState() == ExecutorState.WAITING_FOR_CHILDREN) {
                    childExecutors.stream().filter(c -> c.getActualState() == FINISHED).forEach(this::removeChildExecutor);
                }

                /* if has children then the state can only be WAITING or IDLE*/
                if (hasChildren() && actualState() != ExecutorState.WAITING_FOR_CHILDREN && actualState() != ExecutorState.IDLE) {
                    publishError(exception(StrandExecutorException.class, "[{}] inconsistent state! There are children, so current state can only be IDLE or WAITING FOR CHILDREN, pausing! Current state is {}", strand, actualState()));
                    updateState(ExecutorState.IDLE);
                }

                if (hasChildren()) {
                    boolean allPaused = childExecutors.stream().map(StrandExecutor::getActualState).allMatch(PAUSED::equals);
                    if (allPaused && actualState() != ExecutorState.IDLE) {
                        LOGGER.debug("[{}] paused because all children are paused", strand);
                        updateState(ExecutorState.IDLE);
                    } else if (!allPaused && actualState() != ExecutorState.WAITING_FOR_CHILDREN) {
                        LOGGER.debug("[{}] has some non-paused children. Setting the state to waiting", strand);
                        updateState(ExecutorState.WAITING_FOR_CHILDREN);
                    }
                }

                StrandCommand commandToExecute = commandQueue.poll();

                if (commandToExecute == StrandCommand.PAUSE) {
                    pause();
                }

                if (commandToExecute == StrandCommand.SKIP) {
                    if (hasChildren()) {
                        publishError(new RejectedCommandException(commandToExecute, "[{}] has children so skipping is not allowed", strand));
                    } else {
                        moveNext();
                    }
                }

                if (commandToExecute == StrandCommand.STEP_INTO) {
                    if (hasChildren()) {
                        publishError(new RejectedCommandException(commandToExecute, "[{}] has children so step into is not allowed", strand));
                    } else {
                        stepInto();
                    }
                }

                if (commandToExecute == STEP_OVER && hasChildren()) {
                    publishError(new RejectedCommandException(commandToExecute, "[{}] has children so step over is not allowed", strand));
                }

                if (commandToExecute == STEP_OVER) {
                    currentStepOverSource = actualBlock();
                }

                if (commandToExecute == STEP_OVER || commandToExecute == RESUME) {

                    if (structure.isParallel(actualBlock()) && hasChildren()) {
                        updateState(ExecutorState.WAITING_FOR_CHILDREN);
                        LOGGER.debug("[{}] instructing children to RESUME", strand);
                        childExecutors.forEach(child -> child.instruct(RESUME));
                    } else {
                        if (commandToExecute == STEP_OVER) {
                            updateState(ExecutorState.STEPPING_OVER);
                        } else {
                            updateState(ExecutorState.RESUMING);
                        }
                    }
                }

                if (actualState() == ExecutorState.WAITING_FOR_CHILDREN) {
                    if (!hasChildren()) {
                        if (currentStepOverSource != null) {
                            updateState(ExecutorState.STEPPING_OVER);
                        } else {
                            updateState(ExecutorState.RESUMING);
                        }
                        moveNext();
                    }
                }

                if (actualState() == ExecutorState.STEPPING_OVER) {
                    // FIXME potential performance bottleneck #isDescendantOf is very slow
                    if (!structure.isDescendantOf(actualBlock(), currentStepOverSource)) {
                        // Stepping over has finished the subtree of the block that initiate it.. finishing
                        updateState(ExecutorState.IDLE);
                        currentStepOverSource = null;
                    }
                }

                if (actualState() == ExecutorState.RESUMING || actualState() == ExecutorState.STEPPING_OVER) {

                    if (isLeaf(actualBlock())) {
                        LOGGER.debug("[{}] executing {}", strand, actualBlock());
                        Result result = leafExecutor.execute(actualBlock());
                        if (result == Result.SUCCESS) {
                            moveNext();
                        } else {
                            LOGGER.warn("[{}] execution of {} returned {}. Pausing strand", strand, actualBlock(), result);
                            updateState(ExecutorState.IDLE);
                        }
                    } else if (structure.isParallel(actualBlock())) {
                        for (Block child : structure.childrenOf(actualBlock())) {
                            StrandExecutor childExecutor = createChildStrandExecutor(child);
                            childExecutor.instruct(RESUME);
                        }
                        LOGGER.debug("[{}] waiting for children strand to finish", strand);
                        updateState(ExecutorState.WAITING_FOR_CHILDREN);
                    } else {
                        // Sequential block, moving into
                        moveIntoFirstChild();
                    }
                }

                if (commandToExecute != null) {
                    LOGGER.debug("[{}] consumed command {}", strand, commandToExecute);
                    lastCommandSink.onNext(commandToExecute);
                }
            }

            cycleSleep();
        }

        LOGGER.debug("Executor for strand {} is finished", strand);
        executor.shutdown();
    }

    private void pause() {
        if (hasChildren()) {
            LOGGER.debug("[{}] instructing children to pause", strand);
            childExecutors.forEach(child -> child.instruct(PAUSE));
        } else {
            LOGGER.debug("[{}] paused", strand);
            updateState(ExecutorState.IDLE);
        }
    }

    private void stepInto() {
        if (isLeaf(actualBlock())) {
            LOGGER.debug("[{}] {} is a leaf, stepping into is not allowed", strand, actualBlock());
            return;
        }

        if (structure.isParallel(actualBlock())) {
            for (Block childBlock : structure.childrenOf(actualBlock())) {
                StrandExecutor childExecutor = createChildStrandExecutor(childBlock);
                childExecutor.instruct(StrandCommand.PAUSE);
            }
        } else {
            moveIntoFirstChild();
        }

        updateState(ExecutorState.IDLE);
    }

    private void moveIntoFirstChild() {
        List<Block> children = structure.childrenOf(actualBlock());
        if (children.isEmpty()) {
            throw exception(IllegalStateException.class, "Strand {} cannot move into block {}, no children!", strand.id(), actualBlock());
        }

        Block firstChild = children.get(0);
        updateActualBlock(firstChild);
    }

    private void moveNext() {
        Optional<Block> nextBlock = structure.nextBlock(actualBlock());
        if (nextBlock.isPresent()) {
            updateActualBlock(nextBlock.get());
        } else {
            LOGGER.debug("[{}] {} is the last block. Finished", strand, actualBlock());
            updateState(ExecutorState.FINISHED);
            updateActualBlock(null);
        }
    }

    private StrandExecutor createChildStrandExecutor(Block childBlock) {
        Strand childStrand = strandFactory.createChildStrand(strand);
        StrandExecutor childExecutor = strandExecutorFactory.createStrandExecutor(childStrand, structure.substructure(childBlock));
        addChildExecutor(childExecutor);
        LOGGER.debug("[{}] created child strand {}", strand, childStrand);
        return childExecutor;
    }

    private void addChildExecutor(StrandExecutor childExecutor) {
        updateChildrenExecutors(ImmutableList.<StrandExecutor>builder().addAll(childExecutors).add(childExecutor).build());
    }

    private void removeChildExecutor(StrandExecutor childExecutor) {
        updateChildrenExecutors(childExecutors.stream().filter(e -> !e.equals(childExecutor)).collect(toImmutableList()));
    }

    private void updateChildrenExecutors(ImmutableList<StrandExecutor> newChildren) {
        childExecutors = newChildren;
        childExecutorsSink.onNext(childExecutors);
        updateAllowedCommands();
    }

    private void updateActualBlock(Block newBlock) {
        LOGGER.debug("[{}] block = {}", strand, newBlock);
        // TODO Should we complete the stream if the newBlock is null? (strand execution finished)
        actualBlock.set(newBlock);
        blockSink.onNext(newBlock);
        updateAllowedCommands();
    }

    private void updateState(ExecutorState newState) {
        LOGGER.debug("[{}] state = {}", strand, newState);
        // TODO Should we complete the stream if the new state is FINISHED?
        actualState.set(newState);
        stateSink.onNext(runStateFrom(newState));
        updateAllowedCommands();
    }

    private void updateAllowedCommands() {
        if (actualBlock() == null || actualState() == null) {
            allowedCommands.set(ImmutableSet.of());
            return;
        }

        ImmutableSet.Builder<StrandCommand> builder = ImmutableSet.builder();
        switch (runStateFrom(actualState())) {
            case PAUSED:
                builder.add(RESUME);
                if (!hasChildren()) {
                    builder.add(STEP_OVER, SKIP);

                    if (!isLeaf(actualBlock())) {
                        builder.add(STEP_INTO);
                    }
                }
                break;
            case RUNNING:
                builder.add(PAUSE);
                break;
        }

        allowedCommands.set(builder.build());
    }

    @Override
    public Flux<RunState> getStateStream() {
        return stateStream;
    }

    @Override
    public Flux<Block> getBlockStream() {
        return blockStream;
    }

    @Override
    public Flux<Exception> getErrorsStream() {
        return errorStream;
    }

    @Deprecated
    @VisibleForTesting
    public Flux<StrandCommand> getLastCommandStream() {
        return lastCommandStream;
    }

    @Override
    public Strand getStrand() {
        return strand;
    }

    @Override
    public RunState getActualState() {
        return runStateFrom(actualState());
    }

    @Override
    public Block getActualBlock() {
        return actualBlock();
    }

    @Override
    public Set<StrandCommand> getAllowedCommands() {
        return allowedCommands.get();
    }

    /**
     * TODO think about a command for this!! A parametrized command will also solve the concurrency issues that the implementation below have
     */
    @Deprecated
    @VisibleForTesting
    public void moveTo(Block block) {
        synchronized (cycleLock) {
            if (!structure.contains(block)) {
                throw exception(IllegalArgumentException.class, "Strand {} cannot move to {} as is not part of this tree structure", strand.id(), block);
            }
            if (Trees.doesBlockHaveAParallelParent(block, structure)) {
                throw exception(IllegalArgumentException.class, "Strand {} cannot move to {} as is descendant of a parallel block", strand.id(), block);
            }
            if (actualState() != ExecutorState.IDLE) {
                throw exception(IllegalStateException.class, "Strand {} can move only while in paused state! Currently in {}", strand.id(), actualState());
            }
            updateActualBlock(block);
        }
    }

    @VisibleForTesting
    public Set<StrandExecutor> getChildrenStrandExecutors() {
        synchronized (cycleLock) {
            return ImmutableSet.copyOf(childExecutors);
        }
    }

    private ExecutorState actualState() {
        return actualState.get();
    }

    private Block actualBlock() {
        return actualBlock.get();
    }

    private boolean hasChildren() {
        if (childExecutors == null) {
            return false;
        }
        return !childExecutors.isEmpty();
    }

    private boolean isLeaf(Block block) {
        if (block == null) {
            return false;
        }
        return this.structure.isLeaf(block);
    }

    private void publishError(Exception error) {
        LOGGER.error("[{}] {}: {}", strand, error.getClass().getSimpleName(), error.getMessage());
        errorSink.onNext(error);
    }

    private Scheduler publishingScheduler(String suffix) {
        return Schedulers.newSingle("strand-exec-" + strand.id() + "-" + suffix);
    }

    /**
     * Tweak this parameters will improve the performances a lot. Possibly to be externalized to be configurable...
     */
    private void cycleSleep() {
        try {
            switch (actualState()) {
                case WAITING_FOR_CHILDREN:
                    Thread.sleep(EXECUTOR_SLEEP_MS_WAITING_FOR_CHILDREN);
                    break;
                case IDLE:
                    Thread.sleep(EXECUTOR_SLEEP_MS_IDLE);
                    break;
                default:
                    Thread.sleep(EXECUTOR_SLEEP_MS_DEFAULT);
            }
        } catch (InterruptedException e) {
            throw exception(IllegalStateException.class, "Strand {} thread interrupted!", strand.id(), e);
        }
    }

    private static RunState runStateFrom(ExecutorState state) {
        switch (state) {
            case RESUMING:
            case RUNNING_LEAF:
            case STEPPING_OVER:
            case WAITING_FOR_CHILDREN:
                return RUNNING;
            case IDLE:
                return PAUSED;
            case FINISHED:
                return FINISHED;
        }
        throw exception(IllegalArgumentException.class, "Strand state {} cannot be mapped to a RunState", state);
    }

    /**
     * More detailed internal representation of the executor's state
     */
    private enum ExecutorState {
        IDLE,
        STEPPING_OVER,
        RUNNING_LEAF,
        RESUMING,
        FINISHED,
        WAITING_FOR_CHILDREN;
    }
}
