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
		//sk.util.Threads.sleep(1000);
		
		StrandCommand command = context.commandQueue.poll();
		System.out.println("polled command "+command);
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
				//cannot stepInto -> publish error
				return;
			}
			updateRunStates();
		}
		
		if(command == StrandCommand.STEP_OVER) {
			//add breakpoint to next
			//resume
			//[]maybe do not allow ignore after step_over block
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
		System.out.println(context.getStrand()+"exec paused" +context.currentStackElement());
		
	}
	
	void updateRunStates() {
		context.updateRunStatesForStackElements(RunState.PAUSED);
	}

	@Override
	public void onEnterState() {
		context.updateStrandRunState(RunState.PAUSED);	
	}

}
