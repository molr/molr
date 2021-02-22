package io.molr.mole.core.tree.executor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class ConcurrentStrandExecutorSkipCommandTests extends TimeoutEnabledTest{
	
	@Test
	public void skipNodeWithChildrenButWithoutSuccessor() {
		TestTreeContext context = TestTreeContext.builder(TestMissions.testRepresentation(2, 3)).breakPoints("0.2").build();
		EmitterProcessor<Block> blockProcessor = EmitterProcessor.create();
		Flux<Block> blocks = blockProcessor.cache().publishOn(Schedulers.elastic());
		context.strandExecutor().getBlockStream().subscribe(blockProcessor::onNext, e->{}, blockProcessor::onComplete);
		context.resumeRoot();
		
		blocks.map(Block::id).takeUntil("0.2"::equals).blockLast();
		context.strandExecutor().getStateStream().takeUntil(RunState.PAUSED::equals).blockLast();
		context.strandExecutor().instruct(StrandCommand.SKIP);
		
		context.strandExecutor().getStateStream().blockLast();
		Map<String, RunState> expectedRunStates = NodeStateTestUtils.allFinishedBut(context.treeStructure().allBlocks(), RunState.NOT_STARTED, "0.2", 
				"0.2.0", "0.2.1", "0.2.2");
		Assertions.assertThat(context.treeNodeStates().getRunStates().getSnapshot()).containsAllEntriesOf(expectedRunStates);
		
		Map<String, Result> expectedResultStates = NodeStateTestUtils
				.allOfStateBut(context.treeStructure().allBlocks(), Result.SUCCESS, Result.UNDEFINED, "0.2", 
				"0.2.0", "0.2.1", "0.2.2");
		Assertions.assertThat(context.treeNodeStates().getResultStates().getSnapshot()).containsAllEntriesOf(expectedResultStates);
		
		List<Block> blockList = blockProcessor.collect(Collectors.toList()).block();
		System.out.println(blockList);
		List<Block> blockList2 = blocks.collect(Collectors.toList()).block();
		System.out.println(blockList2);
		
		List<Block> blockList3 = blocks.collect(Collectors.toList()).block();
		System.out.println(blockList3);
	}
	
	@Test
	public void skipSequentialRoot() {
		TestTreeContext context = TestTreeContext.builder(TestMissions.testRepresentation(1, 2)).build();
		context.strandExecutor().instruct(StrandCommand.SKIP);
		context.strandExecutor().getStateStream().blockLast();
		Map<String, RunState> expectedRunStates = Map.of("0", RunState.NOT_STARTED, "0.0", RunState.NOT_STARTED);
		Assertions.assertThat(context.treeNodeStates().getRunStates().getSnapshot()).containsAllEntriesOf(expectedRunStates);
	}
	
	@Test
	public void skipParallelRoot() {
		TestTreeContext context = TestTreeContext.builder(TestMissions.testRepresentation(1, 2)).parallel("0").build();
		context.strandExecutor().instruct(StrandCommand.SKIP);
		context.strandExecutor().getStateStream().blockLast();
		Map<String, RunState> expectedRunStates = Map.of("0", RunState.NOT_STARTED, "0.0", RunState.NOT_STARTED);
		Assertions.assertThat(context.treeNodeStates().getRunStates().getSnapshot()).containsAllEntriesOf(expectedRunStates);
	}
	
//  @Test
//  public void testStepOverLastChildrenAfterStepIntoPausesAtParentSibling() throws InterruptedException {
//      moveRootStrandTo(parallelBlock);
//      instructRootStrandSync(STEP_INTO);
//      rootStrandChildren().forEach(se -> waitUntilStrandStateIs(se, PAUSED));
//      LOGGER.info("Children paused");
//      unlatch(latchA1End, latchB1End, latchB2End);
//      rootStrandChildren().forEach(se -> instructSync(se, STEP_OVER));
//
//      waitUntilRootStrandBlockIs(lastBlock);
//      waitUntilRootStrandStateIs(PAUSED);
//
//      assertThatRootStrandBlock().isEqualTo(lastBlock);
//      assertThatRootStrandState().isNotEqualTo(FINISHED);
//  }
}
