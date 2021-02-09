package io.molr.mole.core.tree;

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

	private final Block block;
	private final Map<Block, ConcurrentStrandExecutorStacked> childExecutors = new HashMap<Block, ConcurrentStrandExecutorStacked>();
	private final Set<ConcurrentStrandExecutorStacked> finishedChildren = new HashSet<>();
	private final Set<Block> toBeExecuted = new HashSet<>();
	private final Queue<Block> waitingForInstantiation = new LinkedBlockingQueue<>();
	private final Set<ConcurrentStrandExecutorStacked> runningExecutors = new HashSet<>();
	private final int concurrencyLimit = 20;
	
	public ExecuteChildrenState(Block block, ConcurrentStrandExecutorStacked context) {
		super(context);
		requireNonNull(block);
		this.block = block;
		context.structure.childrenOf(block).forEach(childBlock -> {
			if(!context.toBeIgnored(childBlock)) {
				toBeExecuted.add(childBlock);
				waitingForInstantiation.add(childBlock);
			}
		});
		
	}
	
	abstract void instructCreatedChild(ConcurrentStrandExecutorStacked executor);
	
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
			resumeChildren();
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
		childExecutors.forEach((block, child)->{
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
