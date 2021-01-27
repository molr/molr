package io.molr.mole.core.tree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static java.util.Objects.requireNonNull;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;

public class ExecuteChildrenState extends StrandExecutionState{

//	Set<ConcurrentStrandExecutorStacked> childExecutors;
	Block block;
	Map<Block, ConcurrentStrandExecutorStacked> childExecutors = new HashMap<Block, ConcurrentStrandExecutorStacked>();
	Set<ConcurrentStrandExecutorStacked> finishedChildren = new HashSet<>();
	
	public ExecuteChildrenState(Block block, ConcurrentStrandExecutorStacked context) {
		super(context);
		requireNonNull(block);
		this.block = block;
		context.structure.childrenOf(block).forEach(childBlock -> {
			ConcurrentStrandExecutorStacked childExecutor = context.createChildStrandExecutor(childBlock);
			childExecutors.put(childBlock, childExecutor);
		});
		
	}
	
	@Override
	public void run() {
		
		StrandCommand command = context.commandQueue.poll();
		if(command == StrandCommand.RESUME) {
			resumeChildren();
		}
		
		childExecutors.forEach((block, childExecutor) -> {
			if(childExecutor.isComplete()) {
				finishedChildren.add(childExecutor);
			}
		});
		
		if(finishedChildren.size() == childExecutors.size()) {
			System.out.println(block + "finished children");
			context.popUntilNextChildAvailableAndPush();
			context.updateRunStates(Map.of(block, RunState.FINISHED));
			context.updateLoopState(new NavigatingState(context));
		}
	}
	
	public void resumeChildren() {
		childExecutors.forEach((block, child)->{
			child.instruct(StrandCommand.RESUME);
		});
	}

	@Override
	public void onEnterState() {
		// TODO Auto-generated method stub
		
	}

}
