package io.molr.mole.core.tree;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;

/*
 * TODO Maybe we should move over breakpoint on resume instead of pausing again until breakpoint is removed
 */
public class PausedState extends StrandExecutionState{
	private final static Set<StrandCommand> ALLOWED_COMMANDS_FOR_LEAVES = ImmutableSet.of(StrandCommand.RESUME,
			StrandCommand.SKIP, StrandCommand.STEP_OVER);
	private final static Set<StrandCommand> ALL_COMMANDS_FOR_NON_LEAVES = ImmutableSet.of(StrandCommand.RESUME,
			StrandCommand.STEP_INTO, StrandCommand.SKIP, StrandCommand.STEP_OVER);


	
	public PausedState(ConcurrentStrandExecutorStacked context) {
		super(context);

	}

	@Override
	public void run() {
		
		StrandCommand command = context.commandQueue.poll();
		if(command != null) {
			context.log("retrieved {} command to be executed from queue", command);
		}
		
		if(command == StrandCommand.RESUME) {
			context.updateLoopState(new NavigatingState(context));
		}
		
		if(command == StrandCommand.STEP_INTO) {
			Block current = context.currentStackElement();
			
			if(context.structure.isParallel(current)) {
				context.updateLoopState(new ExecuteChildrenPausedState(current, context));
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
			Block skipped = context.popStackElement();
			context.updateRunStates(Map.of(skipped, RunState.NOT_STARTED));
			context.popUntilNextChildAvailableAndPush();
			if(context.isStackEmpty()) {
				context.log("PausedState: stack is empty, we are done here");
			}
			updateRunStates();
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
	
	@Override
	public Set<StrandCommand> allowedCommands() {
		if(context.currentStackElementIsLeave()) {
			return ALLOWED_COMMANDS_FOR_LEAVES;
		}
		return ALL_COMMANDS_FOR_NON_LEAVES;

	}

}
