package io.molr.mole.core.tree;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.In;
import io.molr.commons.domain.MissionInput;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.Out;
import io.molr.mole.core.tree.executor.ConcurrentStrandExecutorStacked;
import io.molr.mole.core.tree.executor.StrandExecutorFactoryNew;

/*
 * TODO add ExecutionStrategy ... and other options
 */
public class TestTreeContext {
	
	Set<Block> parallel;
	Set<Block> breakpoints;
	Set<Block> toBeIgnored;
	Set<Block> blocksToFail;
	
	TreeStructure treeStructure;
	Map<Block, BiConsumer<In, Out>> runnables = new HashMap<>();
	TreeNodeStates nodeStates;
	MissionOutputCollector otuputCollector = new ConcurrentMissionOutputCollector();
	LeafExecutor leafExecutor;
	StrandExecutorFactoryNew strandExecutorFactory;
	ConcurrentStrandExecutorStacked strandExecutor;
	
	private TestTreeContext(Builder builder) {
		parallel = builder.parallel.build();
		breakpoints = builder.breakpoints.build();
		toBeIgnored = builder.ignored.build();
		blocksToFail = builder.failing.build();
		
		treeStructure = new TreeStructure(builder.representation, parallel, Map.of());
		
		treeStructure.allBlocks().stream().filter(treeStructure::isLeaf).forEach(block -> {
			if(blocksToFail.contains(block)) {
				runnables.put(block, TestMissions.throwingRunnable(block));
			}
			else {
				runnables.put(block, TestMissions.defaultRunnable(block));
			}
		});
		nodeStates = new TreeNodeStates(treeStructure);
		LatchedBlockExecutor leafExecutor = new LatchedBlockExecutor(runnables, MissionInput.empty(), Map.of(), otuputCollector);
		leafExecutor.unlatchAll();
		this.leafExecutor = leafExecutor;
		strandExecutorFactory = new StrandExecutorFactoryNew(leafExecutor, nodeStates);
		strandExecutor = strandExecutorFactory.createRootStrandExecutor(treeStructure, breakpoints, toBeIgnored, ExecutionStrategy.PROCEED_ON_ERROR);
	}
	
	public static Builder builder(MissionRepresentation representation) {
		return Builder.builder(representation);
	}
	
	static class Builder {
		
		private final MissionRepresentation representation;
		private final ImmutableSet.Builder<Block> parallel = ImmutableSet.builder();
		private final ImmutableSet.Builder<Block> breakpoints = ImmutableSet.builder();
		private final ImmutableSet.Builder<Block> failing = ImmutableSet.builder();
		private final ImmutableSet.Builder<Block> ignored = ImmutableSet.builder();
		
		private Builder(MissionRepresentation representation) {
			this.representation = representation;
		}
		
		static Builder builder(MissionRepresentation representation) {
			return new Builder(representation);
		}
		
		Block[] toBlocks(String ... blockIds) {
			Block[] blocks = new Block[blockIds.length];
			for (int i = 0; i < blockIds.length; i++) {
				blocks[i] = representation.blockOfId(blockIds[i]).get();
			}
			return blocks;
		}
		
		Builder breakPoints(String ... blockIds) {
			return breakPoints(toBlocks(blockIds));
		}
		
		Builder breakPoints(Block ... blocks) {
			this.breakpoints.add(blocks);
			return this;
		}
		
		Builder failingBlocks(String ... blockIds) {
			return failingBlocks(toBlocks(blockIds));
		}
		
		Builder failingBlocks(Block ... blocks) {
			this.failing.add(blocks);
			return this;
		}
		
		Builder ignore(String ... blockIds) {
			return ignore(toBlocks(blockIds));
		}
		
		Builder ignore(Block ... blocks) {
			this.ignored.add(blocks);
			return this;
		}
		
		Builder parallel(String ...blockIds) {
			return parallel(toBlocks(blockIds));
		}
		
		Builder parallel(Block ...blocks) {
			this.parallel.add(blocks);
			return this;
		}
		
		TestTreeContext build(){
			return new TestTreeContext(this);
		}
	}

}
