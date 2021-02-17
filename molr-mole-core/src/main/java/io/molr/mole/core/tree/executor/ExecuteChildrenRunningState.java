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
	}
	
	void instructCreatedChild(ConcurrentStrandExecutorStacked childExecutor) {
		/*
		 * TODO fails if command queue of child is not empty
		 */
		childExecutor.instruct(StrandCommand.RESUME);
	}
	
	@Override
	public void run() {
		super.run();
		if(areAllChildrenPaused()) {
			System.out.println("all paused");
			context.updateLoopState(new ExecuteChildrenPausedState(context, block, childExecutors, finishedChildren, toBeExecuted, waitingForInstantiation, runningExecutors, concurrencyLimit));
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
			pauseChildren();
			/*
			 * TODO remove possible dead ends from WaitingForAllPaused
			 */
			context.updateLoopState(new ExecuteChildrenWaitingForAllPausedState(context, block, childExecutors, finishedChildren, toBeExecuted, waitingForInstantiation, runningExecutors, concurrencyLimit));
		}
	}
	
	@Override
	public void onEnterState() {
		context.updateRunStatesForStackElements(RunState.RUNNING);
		context.updateStrandRunState(RunState.RUNNING);
	}

}
