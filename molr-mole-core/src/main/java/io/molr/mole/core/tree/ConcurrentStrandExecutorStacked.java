package io.molr.mole.core.tree;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.molr.commons.domain.*;
import io.molr.mole.core.runnable.RunStates;
import io.molr.mole.core.tree.exception.RejectedCommandException;
import io.molr.mole.core.tree.exception.StrandExecutorException;
import io.molr.mole.core.utils.ThreadFactories;
import io.molr.mole.core.utils.Trees;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.molr.commons.domain.RunState.*;
import static io.molr.commons.domain.StrandCommand.*;
import static io.molr.commons.util.Exceptions.exception;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * Concurrent (non-blocking) implementation of a {@link StrandExecutor}. Internally all the operations run on a separate
 * thread avoiding to block the {@link #instruct(StrandCommand)} method (or any other for that matter).
 * <p>
 * This class is thread safe
 */
public class ConcurrentStrandExecutorStacked implements StrandExecutor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentStrandExecutorStacked.class);
    private static final int EXECUTOR_SLEEP_MS_IDLE = 50;
    private static final int EXECUTOR_SLEEP_MS_DEFAULT = 10;
    private static final int EXECUTOR_SLEEP_MS_WAITING_FOR_CHILDREN = 25;

    private final Object cycleLock = new Object();

    private final Set<Block> breakpoints;
    private final Set<Block> blocksToBeIgnored;
    
    private final ExecutorService executor;
    final LinkedBlockingQueue<StrandCommand> commandQueue;
    final TreeStructure structure;//TODO
    private final Strand strand;
    private final StrandExecutorFactoryNew strandExecutorFactory;
    private final LeafExecutor leafExecutor;

    private final ReplayProcessor<StrandCommand> lastCommandSink;
    private final Flux<StrandCommand> lastCommandStream;
    private final ReplayProcessor<RunState> stateSink;
    private final Flux<RunState> stateStream;
    private final ReplayProcessor<Block> blockSink;
    private final Flux<Block> blockStream;
    private final EmitterProcessor<Exception> errorSink;
    private final Flux<Exception> errorStream;

    /* AtomicReference guarantee read safety while not blocking using cycleLock for the getters */
    private final AtomicReference<Set<StrandCommand>> allowedCommands;
    private final AtomicReference<ExecutorState> actualState;
    private final AtomicReference<Block> actualBlock;
    private final Block strandRoot;
    private Block lastBlock;

    private Block currentStepOverSource;
    private StrandCommand lastCommand;
    private ImmutableList<StrandExecutor> childExecutors;
    private ExecutionStrategy executionStrategy;
    private AtomicBoolean aborted = new AtomicBoolean(false); 
    //TODO remove field after refactoring
    AtomicBoolean complete = new AtomicBoolean();
    private RunStates runStates;
    
    private static Scheduler cursorScheduler = Schedulers.newParallel("shared-cursor-scheduler", 12);
    private static Scheduler stateStreamscheduler = Schedulers.newParallel("shared-state-stream-scheduler", 12);

    public ConcurrentStrandExecutorStacked(Strand strand, Block actualBlock, TreeStructure structure,
    		StrandExecutorFactoryNew strandExecutorFactory, LeafExecutor leafExecutor,
            Set<Block> breakpoints, Set<Block> blocksToBeIgnored, ExecutionStrategy executionStrategy,
            RunStates runStates) {
        requireNonNull(actualBlock, "actualBlock cannot be null");
        requireNonNull(runStates);
        this.runStates = runStates;
        this.strandRoot = actualBlock;
        this.executionStrategy = executionStrategy;
        this.breakpoints = breakpoints;
        this.blocksToBeIgnored = blocksToBeIgnored;
        this.structure = requireNonNull(structure, "structure cannot be null");
        this.strand = requireNonNull(strand, "strand cannot be null");
        this.strandExecutorFactory = requireNonNull(strandExecutorFactory, "strandExecutorFactory cannot be null");
        this.leafExecutor = requireNonNull(leafExecutor, "leafExecutor cannot be null");

        this.lastCommandSink = ReplayProcessor.cacheLast();
        this.lastCommandStream = lastCommandSink.publishOn(publishingScheduler("last-command"));
        this.errorSink = EmitterProcessor.create();
        this.errorStream = errorSink.publishOn(publishingScheduler("errors"));

        //cursorScheduler = Schedulers.elastic();//publishingScheduler("cursor");
        //stateStreamscheduler = Schedulers.elastic();//publishingScheduler("states");
        //this.stateStream = stateSink.publishOn(stateStreamscheduler).doFinally(signal -> {
            //stateStreamscheduler.dispose();
        //});
        //this.blockStream = blockSink.publishOn(cursorScheduler).doFinally(signal ->{
        //cursorScheduler.dispose();
        //});
        this.stateSink = ReplayProcessor.cacheLast();
        this.stateStream = stateSink.publishOn(stateStreamscheduler);
        this.blockSink = ReplayProcessor.cacheLast();
        this.blockStream = blockSink.publishOn(cursorScheduler);

        this.allowedCommands = new AtomicReference<>();
        this.actualBlock = new AtomicReference<>();
        this.actualState = new AtomicReference<>();
        this.currentStepOverSource = null;
        this.lastCommand = null;

        updateActualBlock(actualBlock);
        push(actualBlock);
        updateState(ExecutorState.IDLE);
        updateChildrenExecutors(ImmutableList.of());

        state = new PausedState(this);
        
        this.commandQueue = new LinkedBlockingQueue<>(1);
        this.executor = Executors.newSingleThreadExecutor(ThreadFactories.namedDaemonThreadFactory("strand" + strand.id() + "-exec-%d"));
        this.executor.submit(this::lifecyleChecked);
    }

    @Override
    public void instruct(StrandCommand command) {
        if (!commandQueue.offer(command)) {
            LOGGER.warn("Command {} cannot be accepted by strand {} because it is processing another command", command, strand);
        }
    }

    private void traverseLeafs(Block block, Consumer<Block> function) {
    	List<Block> children = structure.childrenOf(block);
    	if(children.isEmpty()) {
    		function.accept(block);
    	}
    	else {
    		children.forEach(child -> {
    			traverseLeafs(child, function);
    		});
    	}
    }
    
    private void lifecyleChecked() {
    	try {
			lifecycle();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
    }
    
    Stack<Block> stack = new Stack<>();
    Map<Block, Integer> childIndices = new HashMap<>();
    private StrandExecutionState state = null;
    private Map<Block, Result> results = new HashMap<>();
    
    boolean toBeIgnored(Block block) {
    	return this.blocksToBeIgnored.contains(block);
    }
    
    boolean isBreakpointSet(Block block) {
    	return this.breakpoints.contains(block);
    }
    
    Result runLeaf(Block block) {
    	runStates.put(block, RunState.RUNNING);
    	Result result = leafExecutor.execute(block);
    	runStates.put(block, RunState.FINISHED);
    	return result;
    }
    
    void updateLoopState(StrandExecutionState newState) {
    	state = newState;
    	state.onEnterState();
    }
    
    void updateRunStates(Map<Block, RunState> runStateUpdates) {
    	runStateUpdates.forEach((block, state)->runStates.put(block, state));
    }
    
    private AtomicReference<RunState> strandRunState = new AtomicReference<>(RunState.NOT_STARTED);
    
    void updateStrandRunState(RunState state) {
    	this.strandRunState.set(state);
    }
    
    void push(Block block) {
    	childIndices.put(block, -1);
    	stack.push(block);
    	updateActualBlock(block);
    }
    
    void addBreakpoint(Block block) {
    	breakpoints.add(block);
    	stateSink.onNext(null);//TODO
    }
    
    boolean hasUnfinishedChild(Block block) {
    	int i = childIndices.get(block);
    	List<Block> children = structure.childrenOf(block);
    	if(structure.isLeaf(block) || structure.isParallel(block))//|| is misleading
    		return false;
    	return i < children.size()-1;
    }
        
//    Block findNext() {
//    	int i = stack.size()-1;
//    	Block next=null;
//    	while(i>=0 && next==null) {
//    		Block block = stack.get(i);
//    		if(hasUnfinishedChild(block)){
//    			int childIdx = childIndex.get(block);
//    			structure.childrenOf(block).get(childIdx);    			
//    		}
//    	}
//    }
    
    void popUntilNext() {
    	while(!stack.isEmpty() && !hasUnfinishedChild(stack.peek())) {
    		System.out.println(strand + " pop "+stack.peek());
    		Block popped = stack.pop();
    		if(popped!=null) {
    			results.put(popped, Result.SUCCESS);//TODO
    			runStates.put(popped, RunState.FINISHED);
    		}
    	}
    }
    
    Optional<Block> popUntilNextChildAvailableAndPush() {
    	popUntilNext();
    	if(stack.isEmpty()) {
    		return Optional.empty();
    	}
    	Block parent = stack.peek();
    	int i = childIndices.get(parent);
    	i++;
    	childIndices.put(parent, i);
    	System.out.println("get child of "+parent+" "+i);
    	Block child = structure.childrenOf(parent).get(i);
    	push(structure.childrenOf(parent).get(i));
    	System.out.println(strand + " next child is "+child);
    	return Optional.of(child);
    }
    
    private void lifecycle() {
        boolean finished = false;
        LOGGER.info("Start lifecycle for "+ strand);

        boolean waitingForChildren = false;
        while (!finished) {
         	
            synchronized (cycleLock) {
            	state.run();
            	if(stack.empty()) {
                    //TODO update this by own state?
                    updateStrandRunState(FINISHED);
            		break;
            	}
            }//cycleLock

            cycleSleep();
        }//whileNotFinished

        
        LOGGER.info("Executor for strand {} is finished", strand);
        executor.shutdown();
        LOGGER.info("Close streams for strand {}", strand);

        // leafExecutor.markBlockFinished(strandRoot);
        stateSink.onNext(FINISHED);
        stateSink.onComplete();
        blockSink.onComplete();
        errorSink.onComplete();
        lastCommandSink.onComplete();     
        //TODO seems not to be the right way
        //stateStreamscheduler.dispose();
        //scheduler.dispose kills the scheduler right away. dispose called with flux.doFinally runs on scheduler thread and is defered
        //cursorScheduler.dispose();
        
        //TODO replace root condition
        if(strandRoot.id().equals("0")) {
            strandExecutorFactory.closeStrandsStream();
        }

        //TODO needs to be refactored, field wouldn't be necessary if executor finished means finished
        complete.set(true);
        //errorSink.dispose();
        //lastCommandSink.dispose();
        LOGGER.info(strand + ": all streams closed");
    }

    private void pause() {
        if (hasChildren()) {
            LOGGER.debug("[{}] instructing children to pause", strand);
            childExecutors.stream().filter(se -> se.getActualState() != PAUSED).forEach(child -> child.instruct(PAUSE));
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
            structure.childrenOf(actualBlock()).forEach(this::createChildStrandExecutor);
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


	/*
	 * private void moveNext() { Optional<Block> nextBlock =
	 * structure.nextBlock(actualBlock()); if (nextBlock.isPresent()) {
	 * updateActualBlock(nextBlock.get()); } else {
	 * LOGGER.debug("[{}] {} is the last block. Finished", strand, actualBlock());
	 * updateState(ExecutorState.FINISHED); updateActualBlock(null); } }
	 */
    
    ConcurrentStrandExecutorStacked createChildStrandExecutor(Block childBlock) {
    	ConcurrentStrandExecutorStacked childExecutor = strandExecutorFactory.createChildStrandExecutor(strand, structure.substructure(childBlock), breakpoints, blocksToBeIgnored, executionStrategy);
        addChildExecutor(childExecutor);
        LOGGER.info("[{}] created child strand {}", strand, childExecutor.getStrand());
        return childExecutor;
    }

    private void addChildExecutor(StrandExecutor childExecutor) {
        updateChildrenExecutors(ImmutableList.<StrandExecutor>builder().addAll(childExecutors).add(childExecutor).build());
    }

    private void updateChildrenExecutors(ImmutableList<StrandExecutor> newChildren) {
        childExecutors = newChildren;
        updateAllowedCommands();
    }

    private void updateActualBlock(Block newBlock) {
        LOGGER.debug("[{}] block = {}", strand, newBlock);
        /*
         * TODO Should we complete the stream if the newBlock is null? (strand execution
         * finished)
         */
        if (newBlock != null) {
            if (actualState.get() == ExecutorState.RESUMING) {
                System.out.println("\n\nRunStates omitted");
                if (!structure.isLeaf(newBlock)) {
                    this.runStates.put(newBlock, RunState.RUNNING);
                }
            }
        }
        actualBlock.set(newBlock);
        blockSink.onNext(newBlock);
        updateAllowedCommands();
    }
    
    private void updateState(ExecutorState newState) {
    	LOGGER.info("[{}] state = {}", strand, newState);
		/* TODO Should we complete the stream if the new state is FINISHED? */
        actualState.set(newState);
        updateAllowedCommands();
        stateSink.onNext(runStateFrom(newState));
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

    @Override
    public Strand getStrand() {
        return strand;
    }

    @Override
    public RunState getActualState() {
        //return runStateFrom(actualState());
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

    /**
     * TODO think about a command for this!! A parametrized command will also solve the concurrency issues that the
     * implementation below have
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
        RESUMING,
        FINISHED,
        WAITING_FOR_CHILDREN;
    }

    @Override
    public String toString() {
        return "ConcurrentStrandExecutor{" +
                "strand=" + strand +
                '}';
    }

    @Override
    public void abort() {
        this.aborted.set(true);
        getChildrenStrandExecutors().forEach(child->child.abort());
        
    }

    @Override
    public boolean aborted() {
        return aborted.get();
    }
}
