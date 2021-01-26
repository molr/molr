package io.molr.mole.core.tree;

import java.util.HashMap;
import java.util.List;
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

		sk.util.Threads.sleep(1000);
		
		StrandCommand command = context.commandQueue.poll();
		System.out.println("polled command "+command);
		if(command == StrandCommand.RESUME) {
			context.updateLoopState(new NavigatingState(context));
		}
		
		if(command == StrandCommand.STEP_INTO) {
			Block current = context.stack.peek();
			
			if(context.structure.isParallel(current)) {
				context.updateLoopState(new ExecuteChildrenState(current, context));
				return;
			}
			
			List<Block> children = context.structure.childrenOf(current);
			Block firstChild = children.get(0);
			context.push(firstChild);
			//TODO needs to be 
			context.childIndices.put(current, context.childIndices.get(current)+1);
			//state is PAUSED
			//breakpoint not needed since we do not move anyway
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
			Block skipped = context.stack.pop();
			context.updateRunStates(Map.of(skipped, RunState.NOT_STARTED));
			if(context.popUntilNextChildAvailableAndPush().isPresent()) {
				System.out.println("pushed next");
			}
			else {
				System.out.println("none xext");
			}
			if(context.stack.isEmpty()) {
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
		System.out.println(context.getStrand()+"exec paused" +context.stack.peek());
		
	}
	
	void updateRunStates() {
		Map<Block, RunState> runStates = new HashMap<>();
		context.stack.forEach(stackElement-> {
			runStates.put(stackElement, RunState.PAUSED);
		});
		context.updateRunStates(runStates);
	}

	@Override
	public void onEnterState() {
		context.updateStrandRunState(RunState.PAUSED);	
	}

}
