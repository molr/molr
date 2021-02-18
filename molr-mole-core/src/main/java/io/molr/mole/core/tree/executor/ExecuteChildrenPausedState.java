package io.molr.mole.core.tree.executor;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;

public class ExecuteChildrenPausedState extends ExecuteChildrenState{
	
	/*
	 * serves as a marker to set RunStates according to children states
	 */
	private boolean anyChildRunning = false;
	
	public ExecuteChildrenPausedState(Block block, ConcurrentStrandExecutorStacked context) {
		super(block, context);
	}
	
	public ExecuteChildrenPausedState(ConcurrentStrandExecutorStacked context, Block block,
			Map<Block, ConcurrentStrandExecutorStacked> childExecutors,
			Set<ConcurrentStrandExecutorStacked> finishedChildren, Set<Block> toBeExecuted,
			Queue<Block> waitingForInstantiation, Set<ConcurrentStrandExecutorStacked> runningExecutors,
			int concurrencyLimit) {
		super(context, block, childExecutors, finishedChildren, toBeExecuted, waitingForInstantiation, runningExecutors, concurrencyLimit);
		anyChildRunning = isAnyChildrenRunning();
	}
	
	@Override
		public Set<StrandCommand> allowedCommands() {
			return ImmutableSet.of(StrandCommand.RESUME);
		}

	@Override
	void instructCreatedChild(ConcurrentStrandExecutorStacked childExecutor) {
		/*
		 * no need for instruction in paused state		
		 */
	}
	
	@Override
	public void run() {
		super.run();
		if(isAnyChildrenRunning()) {
			if(!anyChildRunning) {
				context.updateRunStateForStrandAndStackElements(RunState.RUNNING);
				anyChildRunning = true;
			}
		}
		else {
			if(anyChildRunning) {
				context.updateRunStateForStrandAndStackElements(RunState.PAUSED);
				anyChildRunning = false;
			}
		}
	}
	
	@Override
	protected void executeCommand(StrandCommand command) {
		if(command==StrandCommand.RESUME) {
			context.updateLoopState(new ExecuteChildrenRunningState(context, block,
					childExecutors, finishedChildren, toBeExecuted, waitingForInstantiation, runningExecutors, concurrencyLimit));
			resumeChildren();
		}
	}
	
	@Override
	public void onEnterState() {
		//TODO
		System.out.println("entered execute children paused "+this.block);
		context.updateStrandRunState(RunState.PAUSED);
		context.updateRunStatesForStackElements(RunState.PAUSED);
	}

}
