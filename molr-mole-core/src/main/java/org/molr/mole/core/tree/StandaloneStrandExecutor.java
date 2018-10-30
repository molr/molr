package org.molr.mole.core.tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Collections;
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

public class StandaloneStrandExecutor implements StrandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneStrandExecutor.class);
    public static final int EXECUTOR_SLEEP_MS = 10;

    private final ExecutorService executor;
    private final LinkedBlockingQueue<StrandCommand> commandQueue;
    private final TreeStructure structure;
    private final Strand strand;
    private final StrandFactory strandFactory;
    private final StrandExecutorFactory strandExecutorFactory;
    private final LeafExecutor leafExecutor;
    private final ReplayProcessor<RunState> stateSink;
    private final Flux<RunState> stateStream;
    private final ReplayProcessor<Block> blockSink;
    private final Flux<Block> blockStream;
    private final Flux<Set<StrandCommand>> allowedCommandStream;
    private ImmutableList<StrandExecutor> childExecutors;
    private AtomicReference<StrandExecutorState> actualState;
    private AtomicReference<Block> actualBlock;
    private AtomicReference<Block> currentStepOverSource = new AtomicReference<>();

    public StandaloneStrandExecutor(Strand strand, Block actualBlock, TreeStructure structure, StrandFactory strandFactory, StrandExecutorFactory strandExecutorFactory, LeafExecutor leafExecutor) {
        requireNonNull(actualBlock, "actualBlock cannot be null");
        this.structure = requireNonNull(structure, "structure cannot be null");
        this.strand = requireNonNull(strand, "strand cannot be null");
        this.strandFactory = requireNonNull(strandFactory, "strandFactory cannot be null");
        this.strandExecutorFactory = requireNonNull(strandExecutorFactory, "strandExecutorFactory cannot be null");
        this.leafExecutor = requireNonNull(leafExecutor, "leafExecutor cannot be null");
        this.childExecutors = ImmutableList.of();

        this.stateSink = ReplayProcessor.cacheLast();
        this.stateStream = stateSink.publishOn(Schedulers.elastic());
        this.blockSink = ReplayProcessor.cacheLast();
        this.blockStream = blockSink.publishOn(Schedulers.elastic());
        this.allowedCommandStream = Flux.combineLatest(this.stateStream, this.blockStream, this::allowedCommands)
                .sample(Duration.ofMillis(100)); // Trying to avoid sending 2 updates when both state and block change

        this.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("strand" + strand.id() + "-exec-%d").build());
        this.commandQueue = new LinkedBlockingQueue<>(1);

        this.actualBlock = new AtomicReference<>();
        this.actualState = new AtomicReference<>();
        updateActualBlock(actualBlock);
        updateState(StrandExecutorState.IDLE);

        this.executor.submit(this::lifecycle);
    }

    private void lifecycle() {
        while (actualState.get() != StrandExecutorState.FINISHED) {

            StrandCommand nextCommand = commandQueue.poll();

            if (nextCommand == StrandCommand.PAUSE) {
                pause();
                continue;
            }

            if (nextCommand == StrandCommand.SKIP) {
                moveNext();
                continue;
            }

            if (nextCommand == StrandCommand.STEP_INTO) {
                stepInto();
                continue;
            }

            // FIXME possibly merge with the next if..
            if (nextCommand == StrandCommand.STEP_OVER) {
                if (structure.isParallel(actualBlock.get()) && !childExecutors.isEmpty()) {
                    updateState(StrandExecutorState.WAITING_FOR_CHILDREN);
                    childExecutors.forEach(child -> child.instruct(RESUME));
                } else {
                    currentStepOverSource.set(actualBlock.get());
                    updateState(StrandExecutorState.STEPPING_OVER);
                }
            }

            if (nextCommand == StrandCommand.RESUME) {
                if (structure.isParallel(actualBlock.get()) && !childExecutors.isEmpty()) {
                    updateState(StrandExecutorState.WAITING_FOR_CHILDREN);
                    childExecutors.forEach(child -> child.instruct(RESUME));
                } else {
                    updateState(StrandExecutorState.RESUMING);
                }
            }

            if (actualState.get() == StrandExecutorState.STEPPING_OVER) {
                // FIXME potential performance bottleneck #isDescendantOf
                if (!structure.isDescendantOf(actualBlock.get(), currentStepOverSource.get())) {
                    // Stepping over has finished the subtree of the block that initiate it.. finishing
                    updateState(StrandExecutorState.IDLE);
                    currentStepOverSource.set(null);
                }
            }

            if (actualState.get() == StrandExecutorState.WAITING_FOR_CHILDREN) {
                if (childExecutors.isEmpty()) {
                    if (currentStepOverSource.get() != null) {
                        updateState(StrandExecutorState.STEPPING_OVER);
                    } else {
                        updateState(StrandExecutorState.RESUMING);
                    }
                    moveNext();
                } else {
                    continue;
                }
            }

            if (actualState.get() == StrandExecutorState.RESUMING ||
                    actualState.get() == StrandExecutorState.STEPPING_OVER
                    || actualState.get() == StrandExecutorState.WAITING_FOR_CHILDREN) {

                if (structure.isLeaf(actualBlock.get())) {
                    Boolean leafOk = leafExecutor.execute(actualBlock.get());
                    if (leafOk) {
                        moveNext();
                    } else {
                        LOGGER.warn("[{}] execution of {} returned {}. Pausing strand", strand, actualBlock, leafOk);
                        updateState(StrandExecutorState.IDLE);
                    }
                } else if (structure.isParallel(actualBlock.get())) {
                    for (Block child : structure.childrenOf(actualBlock.get())) {
                        StrandExecutor childExecutor = createChildStrandExecutor(child);
                        childExecutor.instruct(RESUME);
                    }
                    LOGGER.debug("[{}] waiting for children strand to finish", strand);
                    updateState(StrandExecutorState.WAITING_FOR_CHILDREN);
                } else {
                    // Sequential block, moving into
                    moveInto();
                }
            }

            try {
                Thread.sleep(EXECUTOR_SLEEP_MS);
            } catch (InterruptedException e) {
                throw new IllegalStateException(strand + " thread interrupted!", e);
            }
        }

        LOGGER.debug("Executor for strand {} is finished", strand);
        executor.shutdown();
    }


    @Override
    public void instruct(StrandCommand command) {
        if (!commandQueue.offer(command)) {
            LOGGER.warn("Command {} cannot be accepted by strand {} because it is processing another command", command, strand);
        }
    }

    private void pause() {
        LOGGER.debug("[{}] paused and instructed children to pause", strand);
        updateState(StrandExecutorState.IDLE);
        childExecutors.forEach(child -> child.instruct(StrandCommand.PAUSE));
    }

    private void stepInto() {
        if (structure.isLeaf(actualBlock.get())) {
            LOGGER.debug("[{}] {} is a leaf, stepping into is not allowed", strand, actualBlock);
            return;
        }

        if (structure.isParallel(actualBlock.get())) {
            for (Block childBlock : structure.childrenOf(actualBlock.get())) {
                StrandExecutor childExecutor = createChildStrandExecutor(childBlock);
                childExecutor.instruct(StrandCommand.PAUSE);
            }

            updateState(StrandExecutorState.IDLE);
            return;
        }

        moveInto();
        updateState(StrandExecutorState.IDLE);
    }

    private StrandExecutor createChildStrandExecutor(Block childBlock) {
        Strand childStrand = strandFactory.createChildStrand(strand);
        StrandExecutor childExecutor = strandExecutorFactory.createStrandExecutor(childStrand, childBlock, structure.substructure(childBlock));
        childExecutor.getStateStream().filter(RunState.FINISHED::equals).subscribe(s -> {
            synchronized (this) { /* must use the same lock as public synchronized methods */
                removeChildExecutor(childExecutor);
            }
        });
        addChildExecutor(childExecutor);
        LOGGER.debug("[{}] created child strand {}", strand, childStrand);
        return childExecutor;
    }

    private StandaloneStrandExecutor newStrandExecutor(Block childBlock, Strand childStrand, TreeStructure treeStructure, LeafExecutor leafExecutor) {
        return new StandaloneStrandExecutor(childStrand, childBlock, treeStructure, strandFactory, strandExecutorFactory, leafExecutor);
    }

    private void moveInto() {
        List<Block> children = structure.childrenOf(actualBlock.get());
        if (children.isEmpty()) {
            throw new IllegalStateException("Cannot move into block " + actualBlock + ", no children!");
        }

        Block firstChild = children.get(0);
        LOGGER.debug("[{}] moved into first child {} (parent is {})", strand, firstChild, actualBlock);
        updateActualBlock(firstChild);
    }

    private void addChildExecutor(StrandExecutor childExecutor) {
        childExecutors = ImmutableList.<StrandExecutor>builder().addAll(childExecutors).add(childExecutor).build();
    }

    private void removeChildExecutor(StrandExecutor childExecutor) {
        childExecutors = childExecutors.stream().filter(e -> !e.equals(childExecutor)).collect(toImmutableList());
    }

    private void moveNext() {
        Optional<Block> nextBlock = structure.nextBlock(actualBlock.get());
        if (nextBlock.isPresent()) {
            updateActualBlock(nextBlock.get());
        } else {
            LOGGER.debug("[{}] {} is the last block. Finished", strand, actualBlock);
            updateState(StrandExecutorState.FINISHED);
            updateActualBlock(null);
        }
    }

    private void updateActualBlock(Block newBlock) {
        LOGGER.debug("[{}] moved to {}", strand, newBlock);
        // TODO Should we complete the stream if the newBlock is null? (strand execution finished)
        blockSink.onNext(newBlock);
        actualBlock.set(newBlock);
    }

    private void updateState(StrandExecutorState newState) {
        LOGGER.debug("[{}] {}", strand, newState);
        // TODO Should we complete the stream if the new state is FINISHED?
        stateSink.onNext(runstateFrom(newState));
        actualState.set(newState);
    }

    private static RunState runstateFrom(StrandExecutorState state) {
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
        throw new IllegalArgumentException("Strand state " + state + " cannot be mapped to a RunState");
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
    public Flux<Set<StrandCommand>> getAllowedCommandStream() {
        return allowedCommandStream;
    }

    @Override
    public RunState getState() {
        return runstateFrom(actualState.get());
    }

    private Set<StrandCommand> allowedCommands(RunState state, Block block) {
        if (block == null) {
            return Collections.emptySet();
        }

        ImmutableSet.Builder<StrandCommand> builder = ImmutableSet.builder();
        switch (state) {
            case PAUSED:
                builder.add(RESUME, STEP_OVER, SKIP);
                if (!structure.isLeaf(block)) {
                    builder.add(STEP_INTO);
                }
                break;
            case RUNNING:
                builder.add(PAUSE);
                break;
        }
        return builder.build();
    }

    public Strand getStrand() {
        return strand;
    }

    private enum StrandExecutorState {
        IDLE,
        STEPPING_OVER,
        RUNNING_LEAF,
        RESUMING,
        FINISHED, WAITING_FOR_CHILDREN;
    }
}
