package io.molr.mole.core.runnable;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.BlockAttribute;
import io.molr.commons.domain.ImmutableMissionRepresentation;
import io.molr.commons.domain.In;
import io.molr.commons.domain.MissionInput;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.Out;
import io.molr.mole.core.tree.TreeStructure;

public class IntantiatedMissionTree {
	
	private final TreeStructure updatedTreeStructure;
	private final Map<Block, MissionInput> blockInputs;
	private final Map<Block, BiConsumer<In, Out>> runnables;
	
	private IntantiatedMissionTree(Builder builder) {
		MissionRepresentation representation = builder.newRepresentationBuilder.build();
		updatedTreeStructure = new TreeStructure(representation, builder.parallelBlocksBuilder.build(), builder.maxConcurrencyBuilder.build());
		this.runnables = builder.updatedRunnablesAfterTraverseBuilder.build();
		this.blockInputs = builder.scopedInputs.build();
	}
		
	public TreeStructure getUpdatedTreeStructure() {
		return updatedTreeStructure;
	}

	public Map<Block, MissionInput> getBlockInputs() {
		return blockInputs;
	}

	public Map<Block, BiConsumer<In, Out>> getRunnables() {
		return runnables;
	}

	public final static class Builder{
		
		private ImmutableMissionRepresentation.Builder newRepresentationBuilder;
		private final ImmutableSet.Builder<Block> parallelBlocksBuilder = ImmutableSet.builder();
		private final ImmutableMap.Builder<Block, Integer> maxConcurrencyBuilder = ImmutableMap.builder();
		private final ImmutableMap.Builder<Block, MissionInput> scopedInputs = ImmutableMap.builder();
		private final ImmutableMap.Builder<Block, BiConsumer<In, Out>> updatedRunnablesAfterTraverseBuilder = ImmutableMap.builder();
		
		/*
		 * TODO builder may be instantiated with root block
		 */
		Builder builder() {
			return new Builder();
		}
		
		public void addBlockAttributes(Block block, Collection<BlockAttribute> attributes) {
			newRepresentationBuilder.addBlockAttributes(block, attributes);
		}
		
		public void addToParallelBlocks(Block block, int maxConcurrency) {
			this.parallelBlocksBuilder.add(block);
			this.maxConcurrencyBuilder.put(block, maxConcurrency);
		}
		
		public void addChild(Block parent, Block child) {
			if(newRepresentationBuilder==null) {
				newRepresentationBuilder = ImmutableMissionRepresentation.builder(parent);
			}
			newRepresentationBuilder.parentToChild(parent, child);
		}
		
		public void addRunnable(Block block, BiConsumer<In, Out> runnable) {
			this.updatedRunnablesAfterTraverseBuilder.put(block, runnable);
		}
		
		public void addBlockInput(Block block, MissionInput scopedInput) {
			this.scopedInputs.put(block, scopedInput);
		}
		
		public IntantiatedMissionTree build() {
			return new IntantiatedMissionTree(this);
		}
		
	}

}
