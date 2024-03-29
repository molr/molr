package io.molr.mole.core.tree.executor;

import static io.molr.commons.domain.RunState.FINISHED;
import static io.molr.commons.util.Exceptions.exception;
import static java.util.Objects.requireNonNull;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.BlockAttribute;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.Strand;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.runnable.ResultStates;
import io.molr.mole.core.runnable.RunStates;
import io.molr.mole.core.tree.LeafExecutor;
import io.molr.mole.core.tree.QueuedCommand;
import io.molr.mole.core.tree.StrandExecutor;
import io.molr.mole.core.tree.TreeNodeStates;
import io.molr.mole.core.tree.TreeStructure;
import io.molr.mole.core.tree.exception.RejectedCommandException;
import io.molr.mole.core.utils.ThreadFactories;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Concurrent (non-blocking) implementation of a {@link StrandExecutor}. Internally all the operations run on a separate
 * thread avoiding to block the {@link #instruct(StrandCommand)} method (or any other for that matter).
 * <p>
 * This class is thread safe
 */
public class ConcurrentStrandExecutor implements StrandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentStrandExecutor.class);
    private static final int EXECUTOR_SLEEP_MS_DEFAULT = 10;

    private final Object cycleLock = new Object();

    private final Set<Block> breakpoints;
    private final Set<Block> blocksToBeIgnored;

    private final ExecutorService executor;
    private final LinkedBlockingQueue<QueuedCommand> commandQueue;
    final TreeStructure structure;//TODO
    private final Strand strand;
    private final StrandExecutorFactory strandExecutorFactory;
    private final LeafExecutor leafExecutor;

    private final Sinks.Many<QueuedCommand> lastCommandSink;
    private final Flux<QueuedCommand> lastCommandStream;
    private final Sinks.Many<RunState> stateSink;
    private final Flux<RunState> stateStream;
    private final Sinks.Many<Block> blockSink;
    private final Flux<Block> blockStream;
    //private final EmitterProcessor<Exception> errorSink;
    private final Sinks.Many<Exception> errorSink;
    private final Flux<Exception> errorStream;

    /* AtomicReference guarantee read safety while not blocking using cycleLock for the getters */
    private final AtomicReference<Set<StrandCommand>> allowedCommands;
    private final AtomicReference<Block> actualBlock;
    final AtomicReference<QueuedCommand> lastCommand = new AtomicReference<>();
    private final Block strandRoot;

    private ImmutableList<StrandExecutor> childExecutors;
    private ExecutionStrategy executionStrategy;
    private AtomicBoolean aborted = new AtomicBoolean(false);
    private AtomicLong commandId = new AtomicLong(0);
    //TODO remove field after refactoring
    private AtomicBoolean complete = new AtomicBoolean();
    private RunStates runStates;
    private ResultStates resultStates;

    private static Scheduler cursorScheduler = Schedulers.newParallel("shared-cursor-scheduler", 12);
    private static Scheduler stateStreamscheduler = Schedulers.newParallel("shared-state-stream-scheduler", 12);

    private Stack<Block> stack = new Stack<>();
    private Map<Block, Integer> childIndices = new HashMap<>();
    private StrandExecutionState state = null;
    private AtomicReference<RunState> strandRunState = new AtomicReference<>(RunState.NOT_STARTED);

    private AtomicReference<Block> stepOverBlock = new AtomicReference<>();
    private Set<Block> poppedBlocks = new HashSet<>();

    public ConcurrentStrandExecutor(Strand strand, Block actualBlock, TreeStructure structure,
            StrandExecutorFactory strandExecutorFactory, LeafExecutor leafExecutor, Set<Block> breakpoints,
            Set<Block> blocksToBeIgnored, ExecutionStrategy executionStrategy, TreeNodeStates treeNodeStates,
            RunState initialState) {
        requireNonNull(actualBlock, "actualBlock cannot be null");
        requireNonNull(treeNodeStates);
        this.runStates = treeNodeStates.getRunStates();
        this.resultStates = treeNodeStates.getResultStates();
        this.strandRoot = actualBlock;
        this.executionStrategy = executionStrategy;
        this.breakpoints = breakpoints;
        this.blocksToBeIgnored = blocksToBeIgnored;
        this.structure = requireNonNull(structure, "structure cannot be null");
        this.strand = requireNonNull(strand, "strand cannot be null");
        this.strandExecutorFactory = requireNonNull(strandExecutorFactory, "strandExecutorFactory cannot be null");
        this.leafExecutor = requireNonNull(leafExecutor, "leafExecutor cannot be null");

        this.lastCommandSink = Sinks.many().replay().latest();
        this.lastCommandStream = lastCommandSink.asFlux().publishOn(publishingScheduler("last-command"));
        //TODO
        this.errorSink = Sinks.many().multicast().onBackpressureBuffer();
        this.errorStream = errorSink.asFlux().publishOn(publishingScheduler("errors"));

        this.stateSink = Sinks.many().replay().latest();
        this.stateStream = stateSink.asFlux().publishOn(stateStreamscheduler);//stateSink;
        this.blockSink = Sinks.many().replay().latest();
        this.blockStream = blockSink.asFlux().publishOn(cursorScheduler);

        this.allowedCommands = new AtomicReference<>();
        this.actualBlock = new AtomicReference<>();

        updateActualBlock(actualBlock);
        push(actualBlock);
        updateChildrenExecutors(ImmutableList.of());

        if (initialState == RunState.RUNNING) {
            state = new NavigatingState(this);
            updateLoopState(new NavigatingState(this));
        } else {
            state = new PausedState(this);
            updateLoopState(new PausedState(this));
        }

        this.commandQueue = new LinkedBlockingQueue<>(1);
        this.executor = Executors
                .newSingleThreadExecutor(ThreadFactories.namedDaemonThreadFactory("strand" + strand.id() + "-exec-%d"));
        this.executor.submit(this::lifecyleChecked);
    }

    @Override
    public long instruct(StrandCommand command) {
        long id = commandId.getAndIncrement();
        if (!commandQueue.offer(new QueuedCommand(command, id))) {
            String message = MessageFormat.format(
                    "Command {0} cannot be accepted by strand {1} because it is processing another command", command,
                    strand);
            LOGGER.warn(message);
            throw new RuntimeException(message);
        }

        log("Instructed with ", command);
        return id;
    }

    private void lifecycle() {
        LOGGER.debug("Start lifecycle for " + strand);

        while (!stack.isEmpty()) {

            synchronized (cycleLock) {

                QueuedCommand command = commandQueue.poll();
                if (command != null) {
                    if (!state.allowedCommands().contains(command.getStrandCommand())) {
                        LOGGER.warn("Command {} not allowed for state {}.", command.getStrandCommand(),
                                state.getClass());
                        errorSink.tryEmitNext(new RejectedCommandException("command '{}' not allowed (id={})",
                                command.getStrandCommand(), command.getCommandId()));
                    }
                    state.executeCommand(command.getStrandCommand());
                    log("Command {} with id={} has been processed ", command.getStrandCommand(),
                            command.getCommandId());
                    lastCommandSink.tryEmitNext(command);
                    lastCommand.set(command);
                }
                state.run();

                if (stack.empty()) {
                    updateLoopState(new CompletedState(this));
                    break;
                }
            }

            cycleSleep();
        }

        LOGGER.debug("Executor for strand {} is finished", strand);
        executor.shutdown();
        complete.set(true);
        closeStreams();
    }

    private void closeStreams() {
        LOGGER.debug("Close streams for strand {}", strand);
        blockSink.tryEmitComplete();
        errorSink.tryEmitComplete();
        lastCommandSink.tryEmitComplete();
        stateSink.tryEmitNext(FINISHED);
        stateSink.tryEmitComplete();
        LOGGER.debug(strand + ": all streams closed");
    }

    private void lifecyleChecked() {
        try {
            lifecycle();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public boolean isComplete() {
        return complete.get();
    }

    boolean toBeIgnored(Block block) {
        return this.blocksToBeIgnored.contains(block);
    }

    boolean isBreakpointSet(Block block) {
        return this.breakpoints.contains(block);
    }

    ExecutionStrategy executionStrategy() {
        return this.executionStrategy;
    }

    ResultStates resultStates() {
        return resultStates;
    }

    Result runLeaf(Block block) {
        runStates.put(block, RunState.RUNNING);
        log("run leaf {}", block);
        Result result = leafExecutor.execute(block);
        resultStates.put(block, result);
        runStates.put(block, RunState.FINISHED);
        return result;
    }

    void updateLoopState(StrandExecutionState newState) {
        log("updateLoopState from {} to {}", state, newState);
        state = newState;
        setAllowedCommands(newState.allowedCommands());
        state.onEnterState();
    }

    void updateRunStatesForStackElements(RunState stateUpdate) {
        stack.forEach(block -> runStates.put(block, stateUpdate));
        stateSink.tryEmitNext(stateUpdate);/* TODO replace */
    }

    void updateRunStates(Map<Block, RunState> runStateUpdates) {
        runStateUpdates.forEach((block, st) -> runStates.put(block, st));
        stateSink.tryEmitNext(
                RunState.NOT_STARTED);/* TODO remove dummy and find better way to update/trigger gatherMissionState */
    }

    @Deprecated
    void updateStrandRunState(RunState st) {
        this.strandRunState.set(st);
    }

    void updateRunStateForStrandAndStackElements(RunState stateUpdate) {
        this.strandRunState.set(stateUpdate);
        stack.forEach(block -> runStates.put(block, stateUpdate));
        stateSink.tryEmitNext(stateUpdate);
    }

    Block currentStackElement() {
        return this.stack.peek();
    }

    boolean currentStackElementIsLeave() {
        Block currentElement = currentStackElement();
        if (currentElement == null) {
            LOGGER.warn("Ask for type of current stack element but was null");
            return false;
        }
        return structure.isLeaf(currentElement);
    }

    void clearStackElementsAndSetResult() {
        log("clear stack");
        stack.forEach(block -> {
            resultStates.put(block, Result.FAILED);
            runStates.put(block, FINISHED);
        });
        this.stack.clear();
    }

    Block popStackElement() {
        Block popped = this.stack.pop();
        return popped;
    }

    boolean isStackEmpty() {
        return stack.isEmpty();
    }

    void push(Block block) {
        childIndices.put(block, -1);
        stack.push(block);
        updateActualBlock(block);
    }

    Optional<Block> moveChildIndexAndPushNextChild(Block block) {
        if (structure.isLeaf(block) || structure.isParallel(block)) {
            return Optional.empty();
        }
        int currentChildIndex = childIndices.get(block);
        List<Block> children = structure.childrenOf(block);
        while (currentChildIndex < children.size() - 1) {
            currentChildIndex++;
            childIndices.put(block, currentChildIndex);
            Block childCandidate = children.get(currentChildIndex);
            if (!blocksToBeIgnored.contains(childCandidate)) {
                push(childCandidate);
                LOGGER.debug(strand + " Found next child for block " + block + " child: " + childCandidate);
                return Optional.of(childCandidate);
            }
            LOGGER.debug(strand + " Ignore block " + childCandidate);
        }
        return Optional.empty();
    }

    void childIndexToLast(Block block) {
        childIndices.put(block, structure.childrenOf(block).size() - 1);
    }

    private void popUntilNext() {
        while (!stack.isEmpty()) {//!hasUnfinishedChild(stack.peek())) {
            if (moveChildIndexAndPushNextChild(stack.peek()).isPresent()) {
                return;
            }

            log(strand + " pop " + stack.peek());
            Block popped = popStackElement();
            poppedBlocks.add(popped);
            if (popped != null) {
                runStates.put(popped, RunState.FINISHED);
                if (!structure.isLeaf(popped)) {
                    boolean allNonIgnoredChildrenWithSuccess = structure.childrenOf(popped).stream().filter(block -> {
                        /* NOTE: this that RunState of aborted missions must be set to FINISHED */
                        return (runStates.of(block) == RunState.FINISHED);
                    }).allMatch(block -> {
                        return resultStates.of(block) == Result.SUCCESS;
                    });
                    Result blockResult = allNonIgnoredChildrenWithSuccess ? Result.SUCCESS : Result.FAILED;
                    resultStates.put(popped, blockResult);
                    log("block {} is finished, result:{}, runState:{}", popped, blockResult, runStates.of(popped));
                    if (!allNonIgnoredChildrenWithSuccess) {
                        log("overall result is due to failed children, last element popped: " + popped);
                    }
                } else {
                    log("bock {} finished: popped leaf " + popped + runStates.of(popped) + " result: "
                            + resultStates.of(popped));
                }

                Result result = resultStates.of(popped);
                if (result == Result.FAILED) {
                    if (executionStrategy() == ExecutionStrategy.ABORT_ON_ERROR) {
                        LOGGER.debug("Abort Strand execution onError " + popped);
                        clearStackElementsAndSetResult();
                        return;
                    }
                    if (executionStrategy() == ExecutionStrategy.PAUSE_ON_ERROR) {
                        LOGGER.debug("Pause Strand execution onError " + popped);
                        if(!stack.isEmpty()) {
                        	if(moveChildIndexAndPushNextChild(stack.peek()).isPresent()) {
                        		runStates.put(stack.peek(), RunState.PAUSED);
                        		updateLoopState(new PausedState(this));
                        	}
                        }
                        //pointer should halt at next leaf or next successor node
                        //moveChildIndexAndPushNextChild(stack.peek());
                        return;
                    }
                    LOGGER.debug("Proceed Strand execution onError");
                    List<BlockAttribute> attributes = structure.missionRepresentation().blockAttributes().get(popped);
                    if (attributes.contains(BlockAttribute.FORCE_ABORT_ON_ERROR)) {
                        LOGGER.debug("Strand execution has been forced to quit at block " + popped);
                        clearStackElementsAndSetResult();
                        return;
                    }
                    if (attributes.contains(BlockAttribute.ON_ERROR_SKIP_SEQUENTIAL_SIBLINGS)) {
                        if (!stack.isEmpty()) {
                            Block parent = stack.peek();
                            childIndexToLast(parent);
                            LOGGER.debug("Siblings of failed block will be skipped.(next children of " + parent + ")");
                        }
                    }
                }
            }
        }
    }

    Optional<Block> popUntilNextChildAvailableAndPush() {
        popUntilNext();
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(stack.peek());
    }

    void addStepOverBlock(Block block) {
        stepOverBlock.set(block);
    }

    Block removeCurrentStepOverBlock() {
        Block currentStepOverBlock = stepOverBlock.get();
        stepOverBlock.set(null);
        return currentStepOverBlock;
    }

    boolean steppingOverFinished() {
        if (stepOverBlock.get() != null) {
            if (poppedBlocks.contains(stepOverBlock.get())) {
                return true;
            }
        }
        return false;
    }

    ConcurrentStrandExecutor createChildStrandExecutor(Block childBlock, RunState initialState) {
        if (blocksToBeIgnored.contains(childBlock)) {
            return null;
        }
        ConcurrentStrandExecutor childExecutor = strandExecutorFactory.createChildStrandExecutor(strand,
                structure.substructure(childBlock), breakpoints, blocksToBeIgnored, executionStrategy, initialState);
        addChildExecutor(childExecutor);
        LOGGER.debug("[{}] created child strand {}", strand, childExecutor.getStrand());
        return childExecutor;
    }

    private void addChildExecutor(StrandExecutor childExecutor) {
        updateChildrenExecutors(
                ImmutableList.<StrandExecutor> builder().addAll(childExecutors).add(childExecutor).build());
    }

    private void updateChildrenExecutors(ImmutableList<StrandExecutor> newChildren) {
        childExecutors = newChildren;
        updateAllowedCommands();
    }

    private void updateActualBlock(Block newBlock) {
        LOGGER.debug("[{}] update block = {}", strand, newBlock);
        /*
         * TODO Should we complete the stream if the newBlock is null? (strand execution
         * finished)
         */
        actualBlock.set(newBlock);
        updateAllowedCommands();
        blockSink.tryEmitNext(newBlock);
    }

    private void setAllowedCommands(Set<StrandCommand> allowedCommandSet) {
        allowedCommands.set(allowedCommandSet);
        log("updated allowed commands to {}", allowedCommandSet);
    }

    private void updateAllowedCommands() {
        if (actualBlock() == null/* || actualState() == null */) {
            setAllowedCommands(ImmutableSet.of());
            LOGGER.warn("called updateAllowedCommands while current block is empty -> setAllowedTo []");
            return;
        }
        /* TODO remove after fixing init and updateAllowedCommands flow */
        if (state == null) {
            LOGGER.warn("called updateAllowedCommands while state is null");
            return;
        }
        log("Legacy updateAllowedCommands() called", state.allowedCommands());
        setAllowedCommands(state.allowedCommands());
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

    @Override
    public Strand getStrand() {
        return strand;
    }

    @Override
    public RunState getActualState() {
        return this.strandRunState.get();
    }

    @Override
    public Block getActualBlock() {
        return actualBlock();
    }

    @Override
    public Set<StrandCommand> getAllowedCommands() {
        return allowedCommands.get();
    }

    public Set<StrandExecutor> getChildrenStrandExecutors() {
        synchronized (cycleLock) {
            return ImmutableSet.copyOf(childExecutors);
        }
    }

    private Block actualBlock() {
        return actualBlock.get();
    }

    void publishError(Exception error) {
        LOGGER.error("[{}] {}: {}", strand, error.getClass().getSimpleName(), error.getMessage());
        errorSink.tryEmitNext(error);
    }

    private Scheduler publishingScheduler(String suffix) {
        return Schedulers.newSingle("strand-exec-" + strand.id() + "-" + suffix);
    }

    /**
     * Tweak this parameters will improve the performances a lot. Possibly to be externalized to be configurable...
     */
    private void cycleSleep() {
        try {
            Thread.sleep(EXECUTOR_SLEEP_MS_DEFAULT);
        } catch (InterruptedException e) {
            throw exception(IllegalStateException.class, "Strand {} thread interrupted!", strand.id(), e);
        }
    }

    @Override
    public String toString() {
        return "ConcurrentStrandExecutor{" + "strand=" + strand + '}';
    }

    @Override
    public void abort() {
        this.aborted.set(true);
        getChildrenStrandExecutors().forEach(child -> child.abort());

    }

    @Override
    public boolean aborted() {
        return aborted.get();
    }

    void log(String message, Object... objects) {
        Object[] concatenated = new Object[objects.length + 1];
        concatenated[0] = strand;
        for (int i = 0; i < objects.length; i++) {
            concatenated[i + 1] = objects[i];
        }
        LOGGER.debug("[{}]:" + message, concatenated);
    }

    int maxConcurrency(Block block) {
        return this.structure.maxConcurrency(block);
    }

    Block strandRoot() {
        return this.strandRoot;
    }

    public Flux<QueuedCommand> getLastCommandStream() {
        return lastCommandStream;
    }
}
