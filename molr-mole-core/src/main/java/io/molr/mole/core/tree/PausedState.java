package io.molr.mole.core.tree;

import io.molr.commons.domain.StrandCommand;

public class PausedState extends StrandExecutionState{

	public PausedState(ConcurrentStrandExecutorStacked context) {
		super(context);

	}

	@Override
	public void run() {

		StrandCommand command = context.commandQueue.poll();
		if(command == StrandCommand.RESUME) {
			context.state = new NavigatingState(context);
		}
		
		if(command == StrandCommand.SKIP) {
			
		}
		
		if(command != null) {
			System.out.println("command");
		}
		System.out.println("exec paused");
		
	}

}
