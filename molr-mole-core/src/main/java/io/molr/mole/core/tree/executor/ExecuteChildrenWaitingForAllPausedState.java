package io.molr.mole.core.tree.executor;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.StrandCommand;

public class ExecuteChildrenWaitingForAllPausedState extends ExecuteChildrenState{

	public ExecuteChildrenWaitingForAllPausedState(ConcurrentStrandExecutorStacked context, Block block,
			Map<Block, ConcurrentStrandExecutorStacked> childExecutors,
			Set<ConcurrentStrandExecutorStacked> finishedChildren, Set<Block> toBeExecuted,
			Queue<Block> waitingForInstantiation, Set<ConcurrentStrandExecutorStacked> runningExecutors,
			int concurrencyLimit) {
		super(context, block, childExecutors, finishedChildren, toBeExecuted, waitingForInstantiation, runningExecutors,
				concurrencyLimit);
	}

	@Override
	public void run() {
		if(areAllChildrenPaused()) {
			context.updateLoopState(new ExecuteChildrenPausedState(context, block, childExecutors, finishedChildren, toBeExecuted, waitingForInstantiation, runningExecutors, concurrencyLimit));
		}
		/*
		 * TODO we must check for finished too
		 */
	}
	
	@Override
	void instructCreatedChild(ConcurrentStrandExecutorStacked executor) {
		/**
		 * TODO no need to instruct created child, method should also be removed from super
		 */
	}

	@Override
	protected void executeCommand(StrandCommand command) {
		/**
		 * TODO consume resume command		
		 */
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
