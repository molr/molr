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

import com.google.common.collect.ImmutableSet;

import static java.util.Objects.requireNonNull;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;

public abstract class ExecuteChildrenState extends StrandExecutionState{

	protected final Block block;
	protected final Map<Block, ConcurrentStrandExecutorStacked> childExecutors;
	protected final Set<ConcurrentStrandExecutorStacked> finishedChildren;
	protected final Set<Block> toBeExecuted;
	protected final Queue<Block> waitingForInstantiation;
	protected final Set<ConcurrentStrandExecutorStacked> runningExecutors;
	protected final int concurrencyLimit;
	
	public ExecuteChildrenState(Block block, ConcurrentStrandExecutorStacked context) {
		super(context);
		childExecutors = new HashMap<Block, ConcurrentStrandExecutorStacked>();
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
	}

	public ExecuteChildrenState(ConcurrentStrandExecutorStacked context, Block block,
			Map<Block, ConcurrentStrandExecutorStacked> childExecutors,
			Set<ConcurrentStrandExecutorStacked> finishedChildren, Set<Block> toBeExecuted,
			Queue<Block> waitingForInstantiation, Set<ConcurrentStrandExecutorStacked> runningExecutors,
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

	abstract void instructCreatedChild(ConcurrentStrandExecutorStacked executor);
	
	abstract void onCommand(StrandCommand command);
	
	private void instantiateAndAddNewChildExecutors() {
		if(!waitingForInstantiation.isEmpty() && runningExecutors.size()<concurrencyLimit) {
			Block nextChild = waitingForInstantiation.poll();
			ConcurrentStrandExecutorStacked childExecutor = context.createChildStrandExecutor(nextChild);
			if(childExecutor!=null) {
				runningExecutors.add(childExecutor);
				childExecutors.put(nextChild, childExecutor);
				instructCreatedChild(childExecutor);
			}
		}
	}

	private void removeCompletedChildExecutors() {
		List<ConcurrentStrandExecutorStacked> justFinished = runningExecutors.stream()
				.filter(ConcurrentStrandExecutorStacked::isComplete).collect(Collectors.toList());
		finishedChildren.addAll(justFinished);
		runningExecutors.removeAll(justFinished);
	}
	
	@Override
	public void run() {
		
		StrandCommand command = context.commandQueue.poll();
		if(command == StrandCommand.RESUME) {
			onCommand(command);
		}

		removeCompletedChildExecutors();
		instantiateAndAddNewChildExecutors();
		//resumeChildren();//TODO
//		childExecutors.forEach((block, childExecutor) -> {
//			if(childExecutor.isComplete()) {
//				finishedChildren.add(childExecutor);
//			}
//		});
		
		if(finishedChildren.size() == childExecutors.size()) {
			AtomicBoolean hasErrors = new AtomicBoolean(false);
			childExecutors.forEach((childBlock, child)->{
				if(context.resultStates().of(childBlock)==Result.FAILED) {
					hasErrors.set(true);
				};
			});
			//remove
			if(hasErrors.get()) {
				context.log("failed {}", block);
				//context.clearStackElementsAndSetResult();
				if(context.executionStrategy()==ExecutionStrategy.ABORT_ON_ERROR) {
					context.clearStackElementsAndSetResult();
					return;
				}
			}
			else {
				context.log("none errors found", block);
			}
			//
			System.out.println(block + "finished children");
			context.popUntilNextChildAvailableAndPush();
			context.updateRunStates(Map.of(block, RunState.FINISHED));
			context.updateLoopState(new NavigatingState(context));
		}
	}
	
	public void resumeChildren() {
		runningExecutors.forEach(child->{
			child.instruct(StrandCommand.RESUME);
		});
	}

	@Override
	public void onEnterState() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Set<StrandCommand> allowedCommands() {
		return ImmutableSet.of();
	}

}
