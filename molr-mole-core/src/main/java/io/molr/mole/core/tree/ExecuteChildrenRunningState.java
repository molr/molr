package io.molr.mole.core.tree;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.StrandCommand;

public class ExecuteChildrenRunningState extends ExecuteChildrenState{
	
	public ExecuteChildrenRunningState(Block block, ConcurrentStrandExecutorStacked context) {
		super(block, context);
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

}
