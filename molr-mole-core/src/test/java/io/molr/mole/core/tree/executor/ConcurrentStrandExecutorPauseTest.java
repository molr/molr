package io.molr.mole.core.tree.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;

public class ConcurrentStrandExecutorPauseTest {
	
	private final MissionRepresentation testRepresentation = TestMissions.testRepresentation(2, 3);
	private final String firstLeafToPause = "0.0.0";
	private final String secondLeafToPause = "0.0.2";
	private final String directSuccessorToFirstPauseBlock = "0.0.1";
	private final String directSuccessorToSecondPauseBlock = "0.1";
	
	@Rule
	public Timeout globalTimeout= new Timeout(5000, TimeUnit.MILLISECONDS);
	
	@Test
	public void instructPauseWhileRunning_whenInstructedWhileExecutingLeaf_PauseAtSucceedingBlock() {
		TestTreeContext context = TestTreeContext.builder(testRepresentation).
				latched(firstLeafToPause, secondLeafToPause).build();
		
		context.resumeRoot();
		context.awaitEntry(firstLeafToPause);
		context.strandExecutor().instruct(StrandCommand.PAUSE);
		context.unlatch(firstLeafToPause);
		waitForBlockAndRunstate(context.strandExecutor(), directSuccessorToFirstPauseBlock, RunState.PAUSED);
		Assertions.assertThat(context.strandExecutor().getActualState()).isEqualTo(RunState.PAUSED);
		Assertions.assertThat(context.strandExecutor().getActualBlock().id()).isEqualTo("0.0.1");
		
		/*
		 * PAUSE while executint last leaf 0.0.2 in subtree 0.0 -> PAUSE at 0.1
		 */
		context.resumeRoot();
		context.awaitEntry(secondLeafToPause);
		context.strandExecutor().instruct(StrandCommand.PAUSE);
		context.unlatch(secondLeafToPause);
		waitForBlockAndRunstate(context.strandExecutor, directSuccessorToSecondPauseBlock, RunState.PAUSED);
		Assertions.assertThat(context.strandExecutor().getActualState()).isEqualTo(RunState.PAUSED);
		Assertions.assertThat(context.strandExecutor().getActualBlock().id()).isEqualTo("0.1");
		
		context.resumeRoot();
		context.strandExecutor().getStateStream().blockLast();
		Map<String, RunState> expectedRunStates = new HashMap<>();
		Map<String, Result> expectedResults = new HashMap<>();
		context.treeStructure.allBlocks().forEach(block->{
			expectedRunStates.put(block.id(), RunState.FINISHED);
			expectedResults.put(block.id(), Result.SUCCESS);
		});
		
		Assertions.assertThat(context.nodeStates.getResultStates().getSnapshot()).containsAllEntriesOf(expectedResults);
		Assertions.assertThat(context.nodeStates.getRunStates().getSnapshot()).containsAllEntriesOf(expectedRunStates);
	}

	private void waitForBlockAndRunstate(ConcurrentStrandExecutor executor, String blockId, RunState runState) {
		executor.getBlockStream().map(Block::id).takeUntil(blockId::equals).blockLast();
		executor.getStateStream().takeUntil(runState::equals).blockLast();
	}

}
