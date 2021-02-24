package io.molr.mole.core.tree.executor;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;

public class ExecuteChildrenRunningState extends ExecuteChildrenState{
	
	private boolean allChildrenPaused = false;
	
	public ExecuteChildrenRunningState(Block block, ConcurrentStrandExecutor context) {
		super(block, context);
	}
	
	public ExecuteChildrenRunningState(ConcurrentStrandExecutor context, Block block,
			Map<Block, ConcurrentStrandExecutor> childExecutors,
			Set<ConcurrentStrandExecutor> finishedChildren, Set<Block> toBeExecuted,
			Queue<Block> waitingForInstantiation, Set<ConcurrentStrandExecutor> runningExecutors,
			int concurrencyLimit) {
		super(context, block, childExecutors, finishedChildren, toBeExecuted, waitingForInstantiation, runningExecutors, concurrencyLimit);
		allChildrenPaused = areAllChildrenPaused();
	}
	
	@Override
	RunState initialStateOfCreatedChild() {
		return RunState.RUNNING;
	}
	
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
	}

}
