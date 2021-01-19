package io.molr.mole.core.runnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableMap;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.RunState;
import io.molr.mole.core.tree.TreeStructure;

public class RunStates {

	private Map<Block, RunState> runStates = new ConcurrentHashMap<>();
	private Map<String, RunState> runStatesByName = new ConcurrentHashMap<>();
	
	public RunStates(TreeStructure structure) {
		structure.allBlocks().forEach(block -> {
			put(block, RunState.NOT_STARTED);
		});
	}
	
	public void put(Block block, RunState runState) {
		runStates.put(block, runState);
		runStatesByName.put(block.id(), runState);
	}
	
	public Map<String, RunState> getSnapshot(){
		return ImmutableMap.copyOf(runStatesByName);
	}
	
	public RunState of(Block block) {
		if(runStates.containsKey(block)) {
			return runStates.get(block);
		}
		throw new IllegalArgumentException("Cannot find RunState for block "+block);
	}
}
