package io.molr.mole.core.tree.executor;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
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
	public Set<StrandCommand> allowedCommands() {
		/*
		 * is PAUSE a vaild command or should it depend on children?
		 */
		return ImmutableSet.of(StrandCommand.PAUSE);
	}

	@Override
	void onCommand(StrandCommand command) {
		/*
		 * TODO Pause children? and switch state		
		 */
	}

}
