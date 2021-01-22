package io.molr.mole.core.tree;

public abstract class StrandExecutionState {
	
	protected ConcurrentStrandExecutorStacked context;
	
	public StrandExecutionState(ConcurrentStrandExecutorStacked context) {
		this.context = context;
	}
	
	public abstract void run();

}
