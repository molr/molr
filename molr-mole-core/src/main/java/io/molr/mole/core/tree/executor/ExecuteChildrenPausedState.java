package io.molr.mole.core.tree.executor;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;

/*
 * TODO is there a need for setting state to running if children are running
 * -> this would imply that child controls parent behavior. E.g.
 * -> should resume in child trigger resume in another paused child or in parent?
 */
public class ExecuteChildrenPausedState extends ExecuteChildrenState{
	
	public ExecuteChildrenPausedState(Block block, ConcurrentStrandExecutorStacked context) {
		super(block, context);
	}
	
	public ExecuteChildrenPausedState(ConcurrentStrandExecutorStacked context, Block block,
			Map<Block, ConcurrentStrandExecutorStacked> childExecutors,
			Set<ConcurrentStrandExecutorStacked> finishedChildren, Set<Block> toBeExecuted,
			Queue<Block> waitingForInstantiation, Set<ConcurrentStrandExecutorStacked> runningExecutors,
			int concurrencyLimit) {
		super(context, block, childExecutors, finishedChildren, toBeExecuted, waitingForInstantiation, runningExecutors, concurrencyLimit);
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
			//System.out.println("NOT ALL ARE PAUSED\n\n\n\n\n"+block);
			context.updateLoopState(new ExecuteChildrenRunningState(context, block, childExecutors, finishedChildren, toBeExecuted, waitingForInstantiation, runningExecutors, concurrencyLimit));
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
