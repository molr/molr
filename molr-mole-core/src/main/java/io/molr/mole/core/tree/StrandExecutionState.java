package io.molr.mole.core.tree;

import static java.util.Objects.requireNonNull;

public abstract class StrandExecutionState {
	
	protected ConcurrentStrandExecutorStacked context;
	
	public StrandExecutionState(ConcurrentStrandExecutorStacked context) {
		requireNonNull(context);
		this.context = context;
	}
	
	public abstract void run();
	
	public abstract void onEnterState();

}
