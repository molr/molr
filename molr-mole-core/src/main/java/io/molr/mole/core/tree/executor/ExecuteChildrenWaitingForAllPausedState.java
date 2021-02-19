package io.molr.mole.core.tree.executor;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.StrandCommand;

public class ExecuteChildrenWaitingForAllPausedState extends ExecuteChildrenState{

	public ExecuteChildrenWaitingForAllPausedState(ConcurrentStrandExecutor context, Block block,
			Map<Block, ConcurrentStrandExecutor> childExecutors,
			Set<ConcurrentStrandExecutor> finishedChildren, Set<Block> toBeExecuted,
			Queue<Block> waitingForInstantiation, Set<ConcurrentStrandExecutor> runningExecutors,
			int concurrencyLimit) {
		super(context, block, childExecutors, finishedChildren, toBeExecuted, waitingForInstantiation, runningExecutors,
				concurrencyLimit);
	}

	@Override
	public void run() {
		if(areAllChildrenPaused()) {
			context.updateLoopState(new ExecuteChildrenPausedState(context, block, childExecutors, finishedChildren, toBeExecuted, waitingForInstantiation, runningExecutors, concurrencyLimit));
			return;
		}
		removeCompletedChildExecutors();
		if(runningExecutors.isEmpty()) {
			context.updateLoopState(new ExecuteChildrenPausedState(context, block, childExecutors, finishedChildren, toBeExecuted, waitingForInstantiation, runningExecutors, concurrencyLimit));
		}
		//System.out.println("waiting for all paused");
		/*
		 * TODO we must check for finished too
		 */
	}
	
	@Override
	void instructCreatedChild(ConcurrentStrandExecutor executor) {
		/**
		 * TODO no need to instruct created child, method should also be removed from super
		 */
	}

	@Override
	protected void executeCommand(StrandCommand command) {
		/**
		 * TODO consume resume command		
		 */
		if(command == StrandCommand.RESUME) {
			//resumeChildren();
			//
		}
	}
	
	@Override
	public void onEnterState() {
		/*
		 * TODO this is not necessary
		 * System.out.println("update now to paused");
		 * context.updateStrandRunState(RunState.PAUSED);
		 * context.updateRunStatesForStackElements(RunState.PAUSED);
		 */
	}
	
	

}
