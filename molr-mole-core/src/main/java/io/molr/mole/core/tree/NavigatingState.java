package io.molr.mole.core.tree;

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
    	if(!context.isStackEmpty()) {
			Block current = context.currentStackElement();
			
			if(context.toBeIgnored(current)) {
				context.popStackElement();
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
					context.popUntilNextChildAvailableAndPush();
				}
			}
    	}
    	else {
			System.out.println("empty stack finished "+context.getStrand());
    	}
	}

	@Override
	public void onEnterState() {
		context.updateRunStatesForStackElements(RunState.RUNNING);
		context.updateStrandRunState(RunState.RUNNING);		
	}

}
