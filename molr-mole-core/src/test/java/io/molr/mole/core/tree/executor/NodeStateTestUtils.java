package io.molr.mole.core.tree.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.RunState;

public class NodeStateTestUtils {

	public static Map<String, RunState> allFinishedBut(Set<Block> allBlocks, RunState alternativeState, String ... blockIds){
		return allOfStateBut(allBlocks, RunState.FINISHED, alternativeState, blockIds);	
	}
	
	public static <T> Map<String, T> allOfStateBut(Set<Block> allBlocks, T defaultState, T alternativeState, String ... blockIds){
		Map<String, T> mapBuilder = new HashMap<>();
		for (int i = 0; i < blockIds.length; i++) {
			mapBuilder.put(blockIds[i], alternativeState);
		}
		allBlocks.forEach(block -> {
			if(!mapBuilder.containsKey(block.id())) {
				mapBuilder.put(block.id(), defaultState);	
			}
		});
		return ImmutableMap.copyOf(mapBuilder);		
	}
	
}
