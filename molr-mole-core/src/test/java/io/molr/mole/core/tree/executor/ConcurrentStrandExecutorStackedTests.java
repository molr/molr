package io.molr.mole.core.tree.executor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.ImmutableMissionRepresentation;
import io.molr.commons.domain.In;
import io.molr.commons.domain.MissionInput;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.tree.ConcurrentMissionOutputCollector;
import io.molr.mole.core.tree.LatchedBlockExecutor;
import io.molr.mole.core.tree.MissionOutputCollector;
import io.molr.mole.core.tree.TreeNodeStates;
import io.molr.mole.core.tree.TreeStructure;
import io.molr.mole.core.tree.executor.ConcurrentStrandExecutorStacked;
import io.molr.mole.core.tree.executor.StrandExecutorFactoryNew;

public class ConcurrentStrandExecutorStackedTests {

    //TreeTracker<Result> resultTracker = TreeTracker.create(structure.missionRepresentation(), Result.UNDEFINED, Result::summaryOf);
    //TreeTracker<RunState> runStateTracker = TreeTracker.create(structure.missionRepresentation(), RunState.NOT_STARTED, RunState::summaryOf);
	
	Map<Block, BiConsumer<In, Out>> runnables = new HashMap<>();
	
	final Block root = Block.idAndText("0", "ROOT");
	final Block block_00 = Block.idAndText("0.0", "LEAF_0.0");
	final Block block_01 = Block.idAndText("0.1", "LEAF_0.1");
	final Block block_02 = Block.idAndText("0.2", "LEAF_0.2");

//	Block block_02 = Block.idAndText("0.2", "BRANCH_0.2");
//	Block block_020 = Block.idAndText("0.2.0", "LEAF_0.2.0");
//	Block block_021 = Block.idAndText("0.2.1", "LEAF_0.2.1");
	
	private Set<Block> breakPoints = ImmutableSet.of();
	private Set<Block> toBeIgnored = ImmutableSet.of(block_01);
	
	MissionRepresentation representation;
	MissionOutputCollector outputCollector = new ConcurrentMissionOutputCollector();
		
	@Before
	public void configure() {
		ImmutableMissionRepresentation.Builder builder = ImmutableMissionRepresentation.builder(root);
		builder.parentToChild(root, block_00);
		builder.parentToChild(root, block_01);
		builder.parentToChild(root, block_02);
		representation = builder.build();
		
		runnables.put(block_00, (in, out)->{throw new RuntimeException("error");});
		runnables.put(block_01, (in, out)->{System.out.println("hello");});
		runnables.put(block_02, (in, out)->{System.out.println("hello");});
		
		outputCollector = new ConcurrentMissionOutputCollector();
	}
	
	@Test
	public void parallelChildIsNotExecutedWhenIgnored() throws InterruptedException {

		TreeStructure structure = new TreeStructure(representation, ImmutableSet.of(), ImmutableMap.of());
		//TODO scoped input refactoring necessary?
		LatchedBlockExecutor leafExecutor = new LatchedBlockExecutor(runnables, MissionInput.empty(), Map.of(), outputCollector);
        TreeNodeStates nodeStates = new TreeNodeStates(structure);
		StrandExecutorFactoryNew strandExecutorFactory = new StrandExecutorFactoryNew(leafExecutor, nodeStates);
		
		ConcurrentStrandExecutorStacked executor = strandExecutorFactory.createRootStrandExecutor(structure, breakPoints, toBeIgnored, ExecutionStrategy.PROCEED_ON_ERROR);
		
		executor.getBlockStream().subscribe(block->{
			System.out.println("_"+block);
		});
		
		executor.instruct(StrandCommand.RESUME);

		leafExecutor.awaitEntry(block_00);
		leafExecutor.unlatch(block_01);
		leafExecutor.unlatch(block_00);
		leafExecutor.unlatch(block_02);

		executor.getStateStream().blockLast();
		executor.getBlockStream().filter(block_01::equals).blockFirst();
		executor.getBlockStream().subscribe(block->{
			System.out.println("block"+block.text());
		});
				
		
		System.out.println(nodeStates.getRunStates().getSnapshot());
		System.out.println(nodeStates.getResultStates().getSnapshot());
		
		//assert that run state for block_01 is not running and result is IGNORED
		Assertions.assertThat(nodeStates.getResultStates().getSnapshot().get(block_01.id())).isEqualTo(Result.UNDEFINED);
		Assertions.assertThat(nodeStates.getRunStates().getSnapshot().get(block_01.id())).isEqualTo(RunState.NOT_STARTED);

		//TODO stepverifier
	}
	
	@Test
	public void parallelChildIsIgnoredOnStepInto() throws InterruptedException {

		TreeStructure structure = new TreeStructure(representation, ImmutableSet.of(root), ImmutableMap.of());
		
		MissionOutputCollector outputCollector = new ConcurrentMissionOutputCollector();

		LatchedBlockExecutor leafExecutor = new LatchedBlockExecutor(runnables, MissionInput.empty(), Map.of(), outputCollector);
        TreeNodeStates nodeStates = new TreeNodeStates(structure);
		StrandExecutorFactoryNew strandExecutorFactory = new StrandExecutorFactoryNew(leafExecutor, nodeStates);
		
		ConcurrentStrandExecutorStacked executor = strandExecutorFactory.createRootStrandExecutor(structure, breakPoints, toBeIgnored, ExecutionStrategy.ABORT_ON_ERROR);

		executor.instruct(StrandCommand.STEP_INTO);
		
		List<Block>strandRootBlocks = strandExecutorFactory.newStrandsStream()
				.map(ConcurrentStrandExecutorStacked.class::cast)
				.map(exec->exec.strandRoot())
				.takeUntil(block -> {
					return block.id().equals(block_02.id());
				}).collectList().block();
		Assertions.assertThat(strandRootBlocks).containsExactly(root, block_00, block_02);
	}
	
	@Test
	public void stepOverRoot() throws InterruptedException {

		System.out.println("step over root test");
		TreeStructure structure = new TreeStructure(representation, ImmutableSet.of(root), ImmutableMap.of());
		
		MissionOutputCollector outputCollector = new ConcurrentMissionOutputCollector();
		
		//TODO scoped input refactoring necessary?
		LatchedBlockExecutor leafExecutor = new LatchedBlockExecutor(runnables, MissionInput.empty(), Map.of(), outputCollector);
        TreeNodeStates nodeStates = new TreeNodeStates(structure);
		StrandExecutorFactoryNew strandExecutorFactory = new StrandExecutorFactoryNew(leafExecutor, nodeStates);
		
		ConcurrentStrandExecutorStacked executor = strandExecutorFactory.createRootStrandExecutor(structure, breakPoints, toBeIgnored, ExecutionStrategy.ABORT_ON_ERROR);
		
//		executor.getBlockStream().subscribe(block->{
//			System.out.println("block "+block);
//		});
		
		executor.instruct(StrandCommand.STEP_OVER);

		leafExecutor.awaitEntry(block_00);
		Thread.sleep(5000);
		leafExecutor.unlatch(block_00);
		leafExecutor.unlatch(block_01);
		
		leafExecutor.unlatch(block_02);
		executor.getStateStream().blockLast();
		
		executor.getBlockStream().filter(block_01::equals).blockFirst();
				
		
		System.out.println(nodeStates.getRunStates().getSnapshot());
		System.out.println(nodeStates.getResultStates().getSnapshot());
		
		//assert that run state for block_01 is not running and result is IGNORED
		Assertions.assertThat(nodeStates.getResultStates().getSnapshot().get(block_01.id())).isEqualTo(Result.UNDEFINED);
		Assertions.assertThat(nodeStates.getRunStates().getSnapshot().get(block_01.id())).isEqualTo(RunState.NOT_STARTED);
	}
	
}
