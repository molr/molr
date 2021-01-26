package io.molr.mole.core.tree;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.StrandCommand;

public class NavigatingState extends StrandExecutionState{

	boolean paused = true;
	
	public NavigatingState(ConcurrentStrandExecutorStacked context) {
		super(context);
	}

	@Override
	public void run() {
		
		TreeStructure structure = context.structure;
		Stack<Block> stack = context.stack;
		Map<Block,Integer> childIndices = context.childIndex;
    	if(!stack.empty()) {
			Block current = stack.peek();
			List<Block> children = structure.childrenOf(current);
			if(structure.isLeaf(current)) {
				//leaf to execute
				System.out.println("execute "+current);
				context.runLeaf(current);
				//update runStates
				stack.pop();
				//
			}
			else {
				if(structure.isParallel(current)) {
					//other executors involved
					context.state = new ExecuteChildrenState(current, context);
					return;
				}
				else {
					int currentChild = context.childIndex.get(current)+1;
					if(children.size()==currentChild) {
						System.out.println("no more children "+current);
						stack.pop();
						//-could also directly move to next
						//- we could also get an result here
						//- what about non executed children
					}
					else {
						Block next = children.get(currentChild);
						context.push(next);
						childIndices.put(current, currentChild);
					}
				}
			}
    	}
    	else {
			System.out.println("empty stack finished "+context.getStrand());
    	}
	}

}
