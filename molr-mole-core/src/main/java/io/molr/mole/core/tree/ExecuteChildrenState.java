package io.molr.mole.core.tree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.molr.commons.domain.Block;

public class ExecuteChildrenState extends StrandExecutionState{

//	Set<ConcurrentStrandExecutorStacked> childExecutors;
	Block block;
	Map<Block, ConcurrentStrandExecutorStacked> childExecutors = new HashMap<Block, ConcurrentStrandExecutorStacked>();
	Set<ConcurrentStrandExecutorStacked> finishedChildren = new HashSet<>();
	
	public ExecuteChildrenState(Block block, ConcurrentStrandExecutorStacked context) {
		super(context);
		context.structure.childrenOf(block).forEach(childBlock -> {
			ConcurrentStrandExecutorStacked childExecutor = context.createChildStrandExecutor(childBlock);
			childExecutors.put(block, childExecutor);
		});
		
	}
	
	@Override
	public void run() {
		childExecutors.forEach((block, childExecutor) -> {
			if(childExecutor.complete.get()) {
				finishedChildren.add(childExecutor);
			}
		});
		
		if(finishedChildren.size() == childExecutors.size()) {
			System.out.println("finished");
			context.stack.pop();
			context.state = new NavigatingState(context);
		}
	}

}
