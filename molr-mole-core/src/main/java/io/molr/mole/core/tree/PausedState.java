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
			context.state = new NavigatingState(context);
		}
		
		if(command == StrandCommand.STEP_INTO) {
			Block current = context.stack.peek();
			
			if(context.structure.isParallel(current)) {
				context.state = new ExecuteChildrenState(current, context);
				return;
			}
			
			List<Block> children = context.structure.childrenOf(current);
			Block firstChild = children.get(0);
			context.push(firstChild);
			//TODO needs to be 
			context.childIndex.put(current, context.childIndex.get(current)+1);
			//state is PAUSED
			//breakpoint not needed since we do not move anyway
			//context.addBreakpoint(firstChild);
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
			Block pointer = context.stack.peek();//TODO ensure stack is not empty -> peek throws exception
			Block skipped = context.stack.pop();
			context.updateRunStates(Map.of(skipped, RunState.NOT_STARTED));
			if(context.pushNextChild()) {
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
			if(context.stack.size()>1) {
				//get next
				Block parent = context.stack.elementAt(context.stack.size()-2);
				int childInex = context.childIndex.get(parent);
				List<Block> childrenOfParent = context.structure.childrenOf(parent);
				if(childInex < childrenOfParent.size()-1) {
					Block next = childrenOfParent.get(childInex+1);
					context.push(next);
					//TODO needs to be refactored, helper
					context.childIndex.put(parent, context.childIndex.get(parent)+1);
					System.out.println("nextBreakpoint "+next);
				}
				else {
					//TODO
					System.out.println("next must be found upwards "+childInex+" "+childrenOfParent);
					//pop current and pop parent
					//TODO do this in a consistent mannor, one pop should be sufficient pop and navigate or so
					context.stack.pop();
					context.stack.pop();
				}
			}
			else {
				System.out.println("finished");
			}
			//System.exit(0);
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

}
