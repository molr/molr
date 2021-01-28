package io.molr.mole.core.tree;

import java.util.Set;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;

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
			
			/*
			 * TODO maybe removed
			 */
			if(context.toBeIgnored(current)) {
				context.popStackElement();
				context.popUntilNextChildAvailableAndPush();
				return;
			}

			if(context.steppingOverFinished()) {
				Block stepOverSource = context.removeCurrentStepOverBlock();
				context.updateLoopState(new PausedState(context));
				context.log("Pause strand since step over sources have been executed {}", stepOverSource);
				return;
			}
			
			if(context.isBreakpointSet(current)) {
				context.updateLoopState(new PausedState(context));
				return;
			}
						
			if(structure.isLeaf(current)) {
				Result result = context.runLeaf(current);
				if(result==Result.FAILED) {
					if(context.executionStrategy()==ExecutionStrategy.ABORT_ON_ERROR) {
						context.clearStackElementsAndSetResult();
						return;
					}
					if(context.executionStrategy()==ExecutionStrategy.PAUSE_ON_ERROR) {
						context.updateLoopState(new PausedState(context));
						return;
					}
				}
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
			
			context.updateRunStatesForStackElements(RunState.RUNNING);
    	}
    	
    	else {
			context.log("NavigatingState finished with empty stack");
    	}
	}
	
	
	@Override
	public void onEnterState() {
		context.log("enter state NavigatingState");
		context.updateRunStatesForStackElements(RunState.RUNNING);
		context.updateStrandRunState(RunState.RUNNING);
	}
	
	@Override
	public Set<StrandCommand> allowedCommands() {
		// TODO Auto-generated method stub
		return super.allowedCommands();
	}

}