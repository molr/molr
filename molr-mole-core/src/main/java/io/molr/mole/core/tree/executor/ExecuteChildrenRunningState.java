package io.molr.mole.core.tree.executor;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;

public class ExecuteChildrenRunningState extends ExecuteChildrenState{
	
	public ExecuteChildrenRunningState(Block block, ConcurrentStrandExecutorStacked context) {
		super(block, context);
	}
	
	public ExecuteChildrenRunningState(ConcurrentStrandExecutorStacked context, Block block,
			Map<Block, ConcurrentStrandExecutorStacked> childExecutors,
			Set<ConcurrentStrandExecutorStacked> finishedChildren, Set<Block> toBeExecuted,
			Queue<Block> waitingForInstantiation, Set<ConcurrentStrandExecutorStacked> runningExecutors,
			int concurrencyLimit) {
		super(context, block, childExecutors, finishedChildren, toBeExecuted, waitingForInstantiation, runningExecutors, concurrencyLimit);
		allChildrenPaused = areAllChildrenPaused();
	}
	
	void instructCreatedChild(ConcurrentStrandExecutorStacked childExecutor) {
		/*
		 * TODO fails if command queue of child is not empty
		 */
		childExecutor.instruct(StrandCommand.RESUME);
	}
	
	boolean allChildrenPaused = false;
	
	@Override
	public void run() {
		super.run();
		if(areAllChildrenPaused()) {
			if(!allChildrenPaused) {
				allChildrenPaused = true;
				context.updateRunStateForStrandAndStackElements(RunState.PAUSED);
			}
		}
		else {
			if(allChildrenPaused) {
				allChildrenPaused = false;
				context.updateRunStateForStrandAndStackElements(RunState.RUNNING);
			}
		}
	}
	
	@Override
	public Set<StrandCommand> allowedCommands() {
		/*
		 * is PAUSE a vaild command or should it depend on children?
		 */
		return ImmutableSet.of(StrandCommand.PAUSE);
	}

	@Override
	protected void executeCommand(StrandCommand command) {
		if(command == StrandCommand.PAUSE) {
			/*
			 * TODO instructing children is dangerous as command queue might not be empty!
			 */
			pauseChildren();
			context.updateLoopState(new ExecuteChildrenPausedState(context, block, childExecutors, finishedChildren, toBeExecuted, waitingForInstantiation, runningExecutors, concurrencyLimit));
		}
	}
	
	@Override
	public void onEnterState() {
		context.updateRunStateForStrandAndStackElements(RunState.RUNNING);
	}

}
