package io.molr.mole.core.tree.executor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;

public abstract class ExecuteChildrenState extends StrandExecutionState{

	protected final Block block;
	protected final Map<Block, ConcurrentStrandExecutor> childExecutors;
	protected final Set<ConcurrentStrandExecutor> finishedChildren;
	protected final Set<Block> toBeExecuted;
	protected final Queue<Block> waitingForInstantiation;
	protected final Set<ConcurrentStrandExecutor> runningExecutors;
	protected final int concurrencyLimit;
	
	public ExecuteChildrenState(Block block, ConcurrentStrandExecutor context) {
		super(context);
		childExecutors = new HashMap<Block, ConcurrentStrandExecutor>();
		finishedChildren = new HashSet<>();
		toBeExecuted = new HashSet<>();
		waitingForInstantiation = new LinkedBlockingQueue<>();
		runningExecutors = new HashSet<>();
		this.concurrencyLimit = context.maxConcurrency(block);
		requireNonNull(block);
		this.block = block;
		context.structure.childrenOf(block).forEach(childBlock -> {
			if(!context.toBeIgnored(childBlock)) {
				toBeExecuted.add(childBlock);
				waitingForInstantiation.add(childBlock);
			}
		});
		instantiateAndAddNewChildExecutors();
	}

	public ExecuteChildrenState(ConcurrentStrandExecutor context, Block block,
			Map<Block, ConcurrentStrandExecutor> childExecutors,
			Set<ConcurrentStrandExecutor> finishedChildren, Set<Block> toBeExecuted,
			Queue<Block> waitingForInstantiation, Set<ConcurrentStrandExecutor> runningExecutors,
			int concurrencyLimit) {
		super(context);
		this.block = block;
		this.childExecutors = childExecutors;
		this.finishedChildren = finishedChildren;
		this.toBeExecuted = toBeExecuted;
		this.waitingForInstantiation = waitingForInstantiation;
		this.runningExecutors = runningExecutors;
		this.concurrencyLimit = concurrencyLimit;
	}

	abstract RunState initialStateOfCreatedChild();
	
	private void instantiateAndAddNewChildExecutors() {
		//
		while(!waitingForInstantiation.isEmpty() && runningExecutors.size()<concurrencyLimit) {
			Block nextChild = waitingForInstantiation.poll();
			ConcurrentStrandExecutor childExecutor = context.createChildStrandExecutor(nextChild, initialStateOfCreatedChild());
			if(childExecutor!=null) {
				runningExecutors.add(childExecutor);
				childExecutors.put(nextChild, childExecutor);
			}
		}
	}

	protected void removeCompletedChildExecutors() {
		List<ConcurrentStrandExecutor> justFinished = runningExecutors.stream()
				.filter(ConcurrentStrandExecutor::isComplete).collect(Collectors.toList());
		finishedChildren.addAll(justFinished);
		runningExecutors.removeAll(justFinished);
	}
	
	@Override
	public void run() {

		removeCompletedChildExecutors();
		instantiateAndAddNewChildExecutors();
		//resumeChildren();//TODO
//		childExecutors.forEach((block, childExecutor) -> {
//			if(childExecutor.isComplete()) {
//				finishedChildren.add(childExecutor);
//			}
//		});
				
		if(finishedChildren.size() == childExecutors.size()) {
			/*
			 * AtomicBoolean hasErrors = new AtomicBoolean(false);
			 * childExecutors.forEach((childBlock, child)->{
			 * if(context.resultStates().of(childBlock)==Result.FAILED) {
			 * hasErrors.set(true); }; });
			 */
			context.popUntilNextChildAvailableAndPush();
			context.updateRunStates(Map.of(block, RunState.FINISHED));
			/**
			 * last command should maybe field for NavigatingState and all other StepOver related state too
			 */
			if(context.lastCommand.get().getStrandCommand()!=StrandCommand.RESUME) {
				context.addStepOverBlock(block);
			}
			context.updateLoopState(new NavigatingState(context));
			return;
		}
	}

	protected boolean isAnyChildrenRunning() {
//		return runningExecutors.stream().map(ConcurrentStrandExecutor::getActualState)
//				.anyMatch(RunState.RUNNING::equals);
		return runningExecutors.stream()
				.map(ConcurrentStrandExecutor::getActualState)
				.anyMatch(runState -> {
					//System.out.println("anyMatch: "+runState);
					return runState.equals(RunState.RUNNING);
				});
	}
	
	protected boolean areAllChildrenPaused() {
		if(runningExecutors.isEmpty()) {
			return false;
		}
		return runningExecutors.stream()
				.map(ConcurrentStrandExecutor::getActualState)
				.allMatch(runState -> {
					return runState.equals(RunState.PAUSED);
				});
	}
	
	public void resumeChildren() {
		instructChildren(StrandCommand.RESUME);
	}
	
	public void pauseChildren() {
		instructChildren(StrandCommand.PAUSE);
	}
	
	public void instructChildren(StrandCommand command) {
		runningExecutors.forEach(child->{
			child.instruct(command);
		});
	}

}
