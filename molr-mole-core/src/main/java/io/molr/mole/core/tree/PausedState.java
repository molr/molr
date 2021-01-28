package io.molr.mole.core.tree;

import java.util.Map;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;

public class PausedState extends StrandExecutionState{

	public PausedState(ConcurrentStrandExecutorStacked context) {
		super(context);

	}

	@Override
	public void run() {
		
		StrandCommand command = context.commandQueue.poll();
		if(command != null) {
			context.log("retrieved command to be executed from queue", command);
		}
		
		if(command == StrandCommand.RESUME) {
			context.updateLoopState(new NavigatingState(context));
		}
		
		if(command == StrandCommand.STEP_INTO) {
			Block current = context.currentStackElement();
			
			if(context.structure.isParallel(current)) {
				context.updateLoopState(new ExecuteChildrenState(current, context));
				return;
			}
			//TODO
			if(context.moveChildIndexAndPushNextChild(current).isEmpty()){
				context.publishError(new RuntimeException("Cannot move into child since none none ignored available"));
				return;
			}
			updateRunStates();
		}
		
		if(command == StrandCommand.STEP_OVER) {
			context.addStepOverBlock(context.currentStackElement());
			context.updateLoopState(new NavigatingState(context));
			return;
			//[]test stepping over parallel
		}
		
		if(command == StrandCommand.SKIP) {
			//SET Breakpoint to next succ and navigate
			Block skipped = context.popStackElement();
			context.updateRunStates(Map.of(skipped, RunState.NOT_STARTED));
			context.popUntilNextChildAvailableAndPush();
			if(context.isStackEmpty()) {
				context.log("PausedState: stack is empty, we are done here");
			}
			updateRunStates();
		}
		
		if(command != null) {
			System.out.println("command");
		}
	}
	
	void updateRunStates() {
		context.updateRunStatesForStackElements(RunState.PAUSED);
	}

	@Override
	public void onEnterState() {
		context.log("enter PAUSED state");
		context.updateStrandRunState(RunState.PAUSED);
		updateRunStates();
	}

}
