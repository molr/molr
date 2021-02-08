package io.molr.mole.core.tree;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
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

}
