package io.molr.mole.core.runnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableMap;

import io.molr.commons.domain.Block;
import io.molr.mole.core.tree.TreeStructure;

public class BlockStates<T> {

	private Map<Block, T> states = new ConcurrentHashMap<>();
	private Map<String, T> statesByName = new ConcurrentHashMap<>();
	
	public BlockStates(TreeStructure structure, T defaultValue) {
		structure.allBlocks().forEach(block -> {
			put(block, defaultValue);
		});
	}
	
	public void put(Block block, T state) {
		states.put(block, state);
		statesByName.put(block.id(), state);
	}
	
	public Map<String, T> getSnapshot(){
		return ImmutableMap.copyOf(statesByName);
	}
	
	public T of(Block block) {
		if(states.containsKey(block)) {
			return states.get(block);
		}
		throw new IllegalArgumentException("Cannot find State for block "+block);
	}
}
