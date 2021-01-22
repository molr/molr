package io.molr.mole.core.tree;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.molr.commons.domain.*;
import io.molr.mole.core.runnable.RunStates;
import io.molr.mole.core.tree.exception.RejectedCommandException;
import io.molr.mole.core.tree.exception.StrandExecutorException;
import io.molr.mole.core.tree.states.ExecuteChildren;
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
            StrandFactory strandFactory, StrandExecutorFactory strandExecutorFactory, LeafExecutor leafExecutor,
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
        this.strandFactory = requireNonNull(strandFactory, "strandFactory cannot be null");
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
    Map<Block, Integer> childIndex = new HashMap<>();
    
    private void push(Block block) {
    	childIndex.put(block, -1);
    	stack.push(block);
    }
    
    private void lifecycle() {
        // FIXME refactor in a more maintainable way, after tests are complete!
        boolean finished = false;
        LOGGER.info("Start lifecycle for "+ strand);

        boolean waitingForChildren = false;
        while (!finished) {
         	
            synchronized (cycleLock) {
            	commandQueue.poll();
            	if(!stack.empty()) {
            		Block current = stack.peek();
            		List<Block> children = structure.childrenOf(current);
            		if(structure.isLeaf(current)) {
            			//leaf to execute
            			System.out.println("execute "+current);
            			stack.pop();
            			//
            		}
            		else {
            			if(structure.isParallel(current)) {
            				//other executors involved
            				if(!waitingForChildren) {//REPLACE
	            				for(Block child : children) {
	            					System.out.println("add child "+child);
	            					createChildStrandExecutor(child);
	            				}
	            				waitingForChildren = true;//REMOVE
            				}
            				else {
            					int completed = 0;
            					for(StrandExecutor childExecutor : childExecutors) {
            						ConcurrentStrandExecutorStacked stacked = (ConcurrentStrandExecutorStacked)childExecutor;
            						if(stacked.complete.get()) {
            							System.out.println("complete "+stacked.getStrand());
            							completed++;
            						}
            					}
            					if(completed == 2) {//remove
            						stack.pop();
            					}
            				}
            			}
            			else {
            				int currentChild = childIndex.get(current)+1;
            				if(children.size()==currentChild) {
            					System.out.println("no more children "+current);
            					stack.pop();
            					//we could also get an result here
            					//what about non executed children
            				}
            				else {
            					Block next = children.get(currentChild);
            					push(next);
            					childIndex.put(current, currentChild);
            				}
            			}
            		}
            	}
            	else {
            		System.out.println("empty stack finished "+strand);
            		break;
            	}
            	if(true) {
            		continue;
            	}
            	
                if(aborted.get()) {
                    updateState(ExecutorState.FINISHED);
                    //TODO cleanup of running/paused children?
                }
                if (actualState() == ExecutorState.FINISHED) {
                    finished = true;
                    continue;
                }

                /*
                 * if block should be ignored ignore subtree by ignoring all children for branches
                 * or ignore the block itself for leafs
                 * afterwards move next
                 */
                if (blocksToBeIgnored.contains(actualBlock.get())) {
                    LOGGER.info("Ignore all leafs in subtree move on " + actualBlock());
                    // traverseLeafs(actualBlock(), leafExecutor::ignoreBlock);
                    moveNext();
                    continue;
                }

                /* remove finished children */
                if (hasChildren() && actualState() == ExecutorState.WAITING_FOR_CHILDREN) {
					childExecutors.stream().filter(c -> {
						/*
						 * TODO avoid this cast, but complete flag should be removed anyway
						 */
						ConcurrentStrandExecutorStacked childExecutor = (ConcurrentStrandExecutorStacked) c;
						return childExecutor.complete.get();
					}).forEach(completedChild -> {
						removeChildExecutor(completedChild);
						/*
						 * TODO abort in a separate step
						 */
						if (completedChild.aborted() && !this.aborted()) {
							updateState(ExecutorState.FINISHED);
							this.aborted.set(true);
						}
					});
                }

                /* if has children then the state can only be WAITING or IDLE*/
                if (hasChildren() && actualState() != ExecutorState.WAITING_FOR_CHILDREN && actualState() != ExecutorState.IDLE) {
                    publishError(exception(StrandExecutorException.class, "[{}] inconsistent state! There are children, so current state can only be IDLE or WAITING FOR CHILDREN, pausing! Current state is {}", strand, actualState()));
                    updateState(ExecutorState.IDLE);
                }

                if (hasChildren()) {
                    boolean allPaused = childExecutors.stream().map(StrandExecutor::getActualState).allMatch(PAUSED::equals);
                    if (allPaused && actualState() != ExecutorState.IDLE) {
                        LOGGER.info("[{}] paused because all children are paused", strand);
                        updateState(ExecutorState.IDLE);
                    } else if (!allPaused && actualState() != ExecutorState.WAITING_FOR_CHILDREN) {
                        LOGGER.info("[{}] has some non-paused children. Setting the state to waiting", strand);
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
                        if(breakpoints.contains(actualBlock())) {
                            LOGGER.debug("block is a breakpoint" + actualBlock);
                            updateState(ExecutorState.IDLE);
                            continue;
                        }
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
                        updateState(ExecutorState.WAITING_FOR_CHILDREN);// TODO is this actualBlock->runState=running
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
                        if (lastCommand == RESUME) {
                            updateState(ExecutorState.RESUMING);
                        } else {
                            updateState(ExecutorState.IDLE);
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

                    if(breakpoints.contains(actualBlock())) {
                        LOGGER.debug("{} is breakpoint -> go to idle", actualBlock);
                        updateState(ExecutorState.IDLE);
                        continue;
                    }
                    
                    if (isLeaf(actualBlock())) {                     
                        LOGGER.debug("[{}] executing {}", strand, actualBlock());
                        runStates.put(actualBlock(), RUNNING);
                        Result result = leafExecutor.execute(actualBlock());
                        runStates.put(actualBlock(), FINISHED);
                        if (result == Result.SUCCESS || executionStrategy == ExecutionStrategy.PROCEED_ON_ERROR) {
                            moveNext();
                        } else {
                            if(executionStrategy == ExecutionStrategy.PAUSE_ON_ERROR) {
                                LOGGER.warn("[{}] execution of {} returned {}. Pausing strand", strand, actualBlock(), result);
                                updateState(ExecutorState.IDLE);
                            }
                            if (executionStrategy == ExecutionStrategy.ABORT_ON_ERROR) {
                                // TODO aborted seems to be wrong here
                                aborted.set(true);
                                // TODO investigate what happens
                                //this.actualBlock.set(null);
                                updateState(ExecutorState.FINISHED);
                                continue;
                            }
                        }
                    } else if (structure.isParallel(actualBlock())) {
                        for (Block child : structure.childrenOf(actualBlock())) {
                        	if(!blocksToBeIgnored.contains(child)) {
                                StrandExecutor childExecutor = createChildStrandExecutor(child);
                                childExecutor.instruct(RESUME);
                        	}
                        }
                        LOGGER.debug("[{}] waiting for children strand to finish", strand);
                        updateState(ExecutorState.WAITING_FOR_CHILDREN);
                    } else {
                        // Sequential block, moving into
                        moveIntoFirstChild();
                    }
                }

                if (commandToExecute != null) {
                    lastCommand = commandToExecute;
                    LOGGER.debug("[{}] consumed command {}", strand, commandToExecute);
                    lastCommandSink.onNext(commandToExecute);
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

    private void traverseNonLeafes(Block block, Consumer<Block> function) {
        List<Block> children = structure.childrenOf(block);
        if (children.isEmpty()) {
            return;
        }
        function.accept(block);
        for (Block child : children) {
            traverseNonLeafes(child, function);
        }
    }
    
    private void goBack(Block from, Block to, RunState state) {
    	if(from.equals(to)) {
    		runStates.put(from, state);
    		System.out.println("go back from "+ from+ to);
    		return;
    	}
    	else {
    		runStates.put(from, state);
    		System.out.println("go back from "+from + to);
    		goBack(structure.parentOf(from).get(), to, state);
    	}
    }
    
    private void markMixedSubtree(Block block) {
        traverseNonLeafes(block, this::markRunningBlockFinished);
    }

    private void markRunningBlockFinished(Block block) {
        if (!runStates.of(block).equals(RunState.NOT_STARTED)) {
            runStates.put(block, FINISHED);
        }
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
    
    private void moveNext() {
    	lastBlock = actualBlock();
        Optional<Block> nextBlock = structure.nextBlock(actualBlock());
        // TODO move next with skip
        while (nextBlock.isPresent() && blocksToBeIgnored.contains(nextBlock.get())) {
            LOGGER.info(strand+": ignore "+nextBlock+" ");
            nextBlock = structure.nextBlock(nextBlock.get());
            // throw new IllegalStateException("skipped");
        }
        if (nextBlock.isPresent()) {
            Block next = nextBlock.get();
            Block parent = structure.parentOf(next).get();
            List<Block> children = structure.childrenOf(parent);
            int childIndex = children.indexOf(next);
            for (int i = 0; i < childIndex; i++) {
                // RUNSTATE should be finished if running
                Block executedSubtree = children.get(i);
                traverseNonLeafes(executedSubtree, this::markRunningBlockFinished);
            }
            LOGGER.info(strand+" NEXT " + next + " LAST " + lastBlock);
            updateActualBlock(nextBlock.get());
        } else {
            traverseNonLeafes(strandRoot, this::markRunningBlockFinished);
            LOGGER.info("[{}] {} is the last block. Finished", strand, actualBlock());
            // updateActualBlock first since block update belongs to mission state
            // corresponding to update
            updateActualBlock(null);
            updateState(ExecutorState.FINISHED);
        }
    }

    private StrandExecutor createChildStrandExecutor(Block childBlock) {
        Strand childStrand = strandFactory.createChildStrand(strand);
        StrandExecutor childExecutor = strandExecutorFactory.createStrandExecutor(childStrand, structure.substructure(childBlock), breakpoints, blocksToBeIgnored, executionStrategy);
        addChildExecutor(childExecutor);
        LOGGER.debug("[{}] created child strand {}", strand, childStrand);
        return childExecutor;
    }

    private void addChildExecutor(StrandExecutor childExecutor) {
        updateChildrenExecutors(ImmutableList.<StrandExecutor>builder().addAll(childExecutors).add(childExecutor).build());
    }

    private void removeChildExecutor(StrandExecutor childExecutor) {
    	LOGGER.info(this.strand+ ": Remove child executor "+childExecutor.getStrand());
        updateChildrenExecutors(childExecutors.stream().filter(e -> !e.equals(childExecutor)).collect(collectingAndThen(toList(), ImmutableList::copyOf)));
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

//    private void updateState(ExecutorState newState) {
//        LOGGER.debug("[{}] state = {}", strand, newState);
//		/* TODO Should we complete the stream if the new state is FINISHED? */
//        actualState.set(newState);
//        stateSink.onNext(runStateFrom(newState));
//        updateAllowedCommands();
//    }
    
    private void updateState(ExecutorState newState) {
    	if(actualBlock() == null) {
    		LOGGER.error("EXCEPTION "+actualBlock());
    		if(lastBlock == null) {
        		throw new IllegalStateException();	
    		}
    		else {
    			System.out.println("GO newState "+lastBlock+"\n\n\n");
    			goBack(lastBlock, strandRoot, RunState.FINISHED);
    	        actualState.set(newState);
    	        updateAllowedCommands();
    	        stateSink.onNext(runStateFrom(newState));
    	        return;
    		}
    	}
        LOGGER.info("[{}] state = {}", strand, newState);
		/* TODO Should we complete the stream if the new state is FINISHED? */
        actualState.set(newState);
        if (newState == ExecutorState.FINISHED) {
        	goBack(actualBlock(), strandRoot, FINISHED);
        }
        if (newState == ExecutorState.IDLE) {
        	goBack(actualBlock(), strandRoot, RunState.PAUSED);
        }
        if (newState == ExecutorState.RESUMING || newState == ExecutorState.WAITING_FOR_CHILDREN) {
        	//TODO right for WaitingForChildren?
        	goBack(actualBlock(), strandRoot, RunState.RUNNING);
        }
        //goBack(actualBlock(), strandRoot, RunState.PAUSED);
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
