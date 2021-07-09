package io.molr.mole.core.tree.executor;

import java.util.List;
import java.util.Set;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.BlockAttribute;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.tree.TreeStructure;

public class NavigatingState extends StrandExecutionState{

	boolean paused = true;
	
	public NavigatingState(ConcurrentStrandExecutor context) {
		super(context);
	}

	@Override
	public void run() {
		
		TreeStructure structure = context.structure;
    	if(!context.isStackEmpty()) {
			Block current = context.currentStackElement();
			
			/*
			 * TODO maybe removed, ignore may only be evaluated prior to pushing
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
						
			System.out.println(context.getStrand().id()+" current"+current);
			
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
					List<BlockAttribute> attributes = context.structure.missionRepresentation().blockAttributes().get(current);
					System.out.println(attributes);
					if(attributes.contains(BlockAttribute.ON_ERROR_FORCE_QUIT)) {
						context.clearStackElementsAndSetResult();
						context.abortParent.set(true);
						return;
					}
					if(attributes.contains(BlockAttribute.ON_ERROR_SKIP_SEQUENTIAL_SIBLINGS)) {
						context.popAndMoveChildIndexToLast();
					}
				}
				System.out.println("call popUntil"+current);
				context.popUntilNextChildAvailableAndPush();
				//must be pop and next otherwise we would pause again if parent is breakpoint
			}
			else {
				/*
				 * Block with parallel children to be executed by other executors
				 */
				if(structure.isParallel(current)) {
					ExecuteChildrenState newExecuteChildrenState = new ExecuteChildrenRunningState(current, context);
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
		context.updateRunStateForStrandAndStackElements(RunState.RUNNING);
	}
	
	@Override
	public Set<StrandCommand> allowedCommands() {
		return Set.of(StrandCommand.PAUSE);
	}

	@Override
	protected void executeCommand(StrandCommand command) {
		if(command == StrandCommand.PAUSE) {
			context.log("execute PAUSE command");
			context.updateLoopState(new PausedState(context));
			return;
		}
	}

}
