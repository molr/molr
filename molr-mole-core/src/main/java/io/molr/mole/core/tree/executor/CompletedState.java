package io.molr.mole.core.tree.executor;

import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;

public class CompletedState extends StrandExecutionState{

	public CompletedState(ConcurrentStrandExecutor context) {
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

	@Override
	protected void executeCommand(StrandCommand command) {
		// TODO Auto-generated method stub
		
	}

}
