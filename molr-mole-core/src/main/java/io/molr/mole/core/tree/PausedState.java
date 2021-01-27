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
			if(context.popUntilNextChildAvailableAndPush().isPresent()) {
				System.out.println("pushed next");
			}
			else {
				System.out.println("none xext");
			}
			if(context.isStackEmpty()) {
				System.out.println("We are done here");
			}
			updateRunStates();
			if(true) {
				return;
			}
		}
		
		if(command != null) {
			System.out.println("command");
		}
		//System.out.println(context.getStrand()+"exec paused" +context.currentStackElement());
		
	}
	
	void updateRunStates() {
		context.updateRunStatesForStackElements(RunState.PAUSED);
	}

	@Override
	public void onEnterState() {
		context.updateStrandRunState(RunState.PAUSED);	
	}

}
