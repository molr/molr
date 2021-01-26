package io.molr.mole.core.tree;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.RunState;

public class NavigatingState extends StrandExecutionState{

	boolean paused = true;
	
	public NavigatingState(ConcurrentStrandExecutorStacked context) {
		super(context);
	}

	@Override
	public void run() {
		
		TreeStructure structure = context.structure;
		Stack<Block> stack = context.stack;
    	if(!stack.empty()) {
			Block current = stack.peek();
			
			if(context.toBeIgnored(current)) {
				context.stack.pop();
				context.popUntilNextChildAvailableAndPush();
				return;
			}
			
			if(context.isBreakpointSet(current)) {
				context.updateLoopState(new PausedState(context));
				return;
			}
						
			if(structure.isLeaf(current)) {
				context.runLeaf(current);
				context.popUntilNextChildAvailableAndPush();
				//must be pop and next otherwise we would pause again if parent is breakpoint
			}
			else {
				/*
				 * Block with parallel children to be executed by other executors
				 */
				if(structure.isParallel(current)) {
					ExecuteChildrenState newExecuteChildrenState = new ExecuteChildrenState(current, context);
					newExecuteChildrenState.resumeChildren();
					context.updateLoopState(newExecuteChildrenState);
					return;
				}
				/*
				 * Block with children to be executed by own strand
				 */
				else {
					/*
					 * The current block is finished
					 */
					if(!context.hasUnfinishedChild(current)) {
						context.popUntilNextChildAvailableAndPush();
						context.updateRunStates(Map.of(current, RunState.FINISHED));
						//- we could also get an result here
						//- what about non executed children
					}
					/*
					 * The current block has non-executed children
					 */
					else {
						Block next = context.popUntilNextChildAvailableAndPush().get();
						//context.updateRunStates(Map.of(next, RunState.RUNNING));

					}
				}
			}
    	}
    	else {
			System.out.println("empty stack finished "+context.getStrand());
    	}
	}

	@Override
	public void onEnterState() {
		context.updateStrandRunState(RunState.RUNNING);		
	}

}
