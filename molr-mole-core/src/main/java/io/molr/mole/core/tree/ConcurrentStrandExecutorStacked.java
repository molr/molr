package io.molr.mole.core.tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.molr.commons.domain.*;
import io.molr.mole.core.runnable.ResultStates;
import io.molr.mole.core.runnable.RunStates;
import io.molr.mole.core.utils.ThreadFactories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.molr.commons.domain.RunState.*;
import static io.molr.commons.util.Exceptions.exception;
import static java.util.Objects.requireNonNull;

/**
 * Concurrent (non-blocking) implementation of a {@link StrandExecutor}. Internally all the operations run on a separate
 * thread avoiding to block the {@link #instruct(StrandCommand)} method (or any other for that matter).
 * <p>
 * This class is thread safe
 */
public class ConcurrentStrandExecutorStacked implements StrandExecutor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentStrandExecutorStacked.class);
    //private static final int EXECUTOR_SLEEP_MS_IDLE = 50;
    private static final int EXECUTOR_SLEEP_MS_DEFAULT = 10;
    //private static final int EXECUTOR_SLEEP_MS_WAITING_FOR_CHILDREN = 25;

    private final Object cycleLock = new Object();

    private final Set<Block> breakpoints;
    private final Set<Block> blocksToBeIgnored;
    
    private final ExecutorService executor;
    final LinkedBlockingQueue<StrandCommand> commandQueue;
    final TreeStructure structure;//TODO
    private final Strand strand;
    private final StrandExecutorFactoryNew strandExecutorFactory;
    private final LeafExecutor leafExecutor;

    //private final ReplayProcessor<StrandCommand> lastCommandSink;
    private final ReplayProcessor<RunState> stateSink;
    private final Flux<RunState> stateStream;
    private final ReplayProcessor<Block> blockSink;
    private final Flux<Block> blockStream;
    private final EmitterProcessor<Exception> errorSink;
    private final Flux<Exception> errorStream;

    /* AtomicReference guarantee read safety while not blocking using cycleLock for the getters */
    private final AtomicReference<Set<StrandCommand>> allowedCommands;
    private final AtomicReference<Block> actualBlock;
    private final Block strandRoot;

    private ImmutableList<StrandExecutor> childExecutors;
    private ExecutionStrategy executionStrategy;
    private AtomicBoolean aborted = new AtomicBoolean(false); 
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

    public ConcurrentStrandExecutorStacked(Strand strand, Block actualBlock, TreeStructure structure,
    		StrandExecutorFactoryNew strandExecutorFactory, LeafExecutor leafExecutor,
            Set<Block> breakpoints, Set<Block> blocksToBeIgnored, ExecutionStrategy executionStrategy,
            TreeNodeStates treeNodeStates) {
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

        //this.lastCommandSink = ReplayProcessor.cacheLast();
        //this.lastCommandStream = lastCommandSink.publishOn(publishingScheduler("last-command"));
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

        updateActualBlock(actualBlock);
        push(actualBlock);
        updateChildrenExecutors(ImmutableList.of());

        state = new PausedState(this);
        updateLoopState(new PausedState(this));
        
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
    
    private void lifecyleChecked() {
    	try {
			lifecycle();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
    }
    
    boolean isComplete() {
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
    
    ResultStates resultStates(){
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
    	state = newState;
    	setAllowedCommands(newState.allowedCommands());
    	state.onEnterState();
    }
    
    void updateRunStatesForStackElements(RunState stateUpdate) {
    	stack.forEach(block -> runStates.put(block, stateUpdate));
    	stateSink.onNext(stateUpdate);/*TODO replace*/
    }
    
    void updateRunStates(Map<Block, RunState> runStateUpdates) {
    	runStateUpdates.forEach((block, state)->runStates.put(block, state));
    	stateSink.onNext(RunState.NOT_STARTED);/*TODO remove dummy and find better way to update/trigger gatherMissionState */
    }
    
    void updateStrandRunState(RunState state) {
    	this.strandRunState.set(state);
    }
    
    Block currentStackElement() {
    	return this.stack.peek();
    }
    
    boolean currentStackElementIsLeave() {
    	Block currentElement = currentStackElement();
    	if(currentElement == null) {
    		LOGGER.warn("Ask for type of current stack element but was null");
    		return false;
    	}
    	return structure.isLeaf(currentElement);
    }
    
    void clearStackElementsAndSetResult(){
    	log("clear stack");
    	stack.forEach(block-> {
    		resultStates.put(block, Result.FAILED);
    		runStates.put(block, FINISHED);
    	});
    	this.stack.clear();
    }
    
    Block popStackElement() {
    	return this.stack.pop();
    }
    
    boolean isStackEmpty() {
    	return stack.isEmpty();
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
    
//    boolean hasUnfinishedChild(Block block) {
//    	int i = childIndices.get(block);
//    	List<Block> children = structure.childrenOf(block);
//    	if(structure.isLeaf(block) || structure.isParallel(block))//|| is misleading
//    		return false;
//    	return i < children.size()-1;
//    }
    
    Optional<Block> moveChildIndexAndPushNextChild(Block block) {
    	if(structure.isLeaf(block) || structure.isParallel(block)) {
    		return Optional.empty();
    	}
    	int currentChildIndex = childIndices.get(block);
    	List<Block> children = structure.childrenOf(block);
    	while(currentChildIndex < children.size()-1) {
    		currentChildIndex++;
    		childIndices.put(block, currentChildIndex);
    		Block childCandidate = children.get(currentChildIndex);
    		if(!blocksToBeIgnored.contains(childCandidate)) {
    			push(childCandidate);
    			LOGGER.info(strand + " Found next child for block "+block+" child: "+childCandidate);
    			return Optional.of(childCandidate);
    		}
    		else {
    			LOGGER.info(strand + " Ignore block "+childCandidate);
    		}
    	}
    	return Optional.empty();
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
    
    private void popUntilNext() {
    	while(!stack.isEmpty()){//!hasUnfinishedChild(stack.peek())) {
    		if(moveChildIndexAndPushNextChild(stack.peek()).isPresent()) {
    			return;
    		}
    		
    		System.out.println(strand + " pop "+stack.peek());
    		Block popped = stack.pop();
    		poppedBlocks.add(popped);
    		if(popped!=null) {
    			runStates.put(popped, RunState.FINISHED);
    			if(!structure.isLeaf(popped)) {
        			boolean allNonIgnoredChildrenWithSuccess = structure.childrenOf(popped).stream().filter(block -> {
        				/* NOTE: this that RunState of aborted missions must be set to FINISHED*/
        				return (runStates.of(block) == RunState.FINISHED);
        			}).allMatch(block -> {
        				return resultStates.of(block) == Result.SUCCESS;
        			});
        			Result blockResult = allNonIgnoredChildrenWithSuccess?Result.SUCCESS:Result.FAILED;
        			resultStates.put(popped, blockResult);
        			log("block {} is finished, result:{}, runState:{}", popped, blockResult, runStates.of(popped));
        			if(!allNonIgnoredChildrenWithSuccess) {
        				System.out.println("overall failed "+popped);
        			}
    			}
    		}
    	}
    }
    
    Optional<Block> popUntilNextChildAvailableAndPush() {
    	popUntilNext();
    	if(stack.isEmpty()) {
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
    	if(stepOverBlock.get()!=null) {
    		if(poppedBlocks.contains(stepOverBlock.get())) {
    			return true;
    		}
    	}
    	return false;
    }
        
    private void lifecycle() {
        boolean finished = false;
        LOGGER.info("Start lifecycle for "+ strand);

        while (!finished) {
         	
            synchronized (cycleLock) {
            	state.run();
            	/*
            	 * TODO cleanup the cleanup ;)
            	 */
            	if(stack.empty()) {
                    //TODO update this by states itself?
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
        //lastCommandSink.onComplete();  
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
        LOGGER.debug("[{}] update block = {}", strand, newBlock);
        /*
         * TODO Should we complete the stream if the newBlock is null? (strand execution
         * finished)
         */
        actualBlock.set(newBlock);
        updateAllowedCommands();
        blockSink.onNext(newBlock);
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
		/*TODO remove after fixing init and updateAllowedCommands flow*/
        if(state==null) {
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

    private Set<StrandExecutor> getChildrenStrandExecutors() {
        synchronized (cycleLock) {
            return ImmutableSet.copyOf(childExecutors);
        }
    }

    private Block actualBlock() {
        return actualBlock.get();
    }

    void publishError(Exception error) {
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
        	Thread.sleep(EXECUTOR_SLEEP_MS_DEFAULT);
        } catch (InterruptedException e) {
            throw exception(IllegalStateException.class, "Strand {} thread interrupted!", strand.id(), e);
        }
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
    
    void log(String message, Object ... objects){
    	Object[] concatenated = new Object[objects.length+1];
    	concatenated[0] = strand;
    	for (int i = 0; i < objects.length; i++) {
			concatenated[i+1] = objects[i];
		}
    	LOGGER.info("[{}]:"+message, concatenated);
    }
}
