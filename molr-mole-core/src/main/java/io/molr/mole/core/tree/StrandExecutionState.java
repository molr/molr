package io.molr.mole.core.tree;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.StrandCommand;

public abstract class StrandExecutionState {
	
	protected ConcurrentStrandExecutorStacked context;
	
	public StrandExecutionState(ConcurrentStrandExecutorStacked context) {
		requireNonNull(context);
		this.context = context;
	}
	
	public abstract void run();
	
	public abstract void onEnterState();
	
	public Set<StrandCommand> allowedCommands(){
		return ImmutableSet.of();
	}

}
