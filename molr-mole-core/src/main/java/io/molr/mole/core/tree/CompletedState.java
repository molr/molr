package io.molr.mole.core.tree;

import io.molr.commons.domain.RunState;

public class CompletedState extends StrandExecutionState{

	public CompletedState(ConcurrentStrandExecutorStacked context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEnterState() {
		context.updateStrandRunState(RunState.FINISHED);		
	}

}
