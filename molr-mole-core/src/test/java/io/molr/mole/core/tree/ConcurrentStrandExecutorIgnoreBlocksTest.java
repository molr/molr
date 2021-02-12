package io.molr.mole.core.tree;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class ConcurrentStrandExecutorIgnoreBlocksTest {
	
	/*
	 * e.g. test tree with depthOfLeafes=2 and childrenPerBranch=3
	 * 0
	 *  0.0
	 *   0.0.0
	 *   0.0.1
	 *   0.0.2
	 *  0.1
	 *   ...
	 *  0.2
	 *   ... 
	 */
	private MissionRepresentation defaultRepresentation = TestMissions.testRepresentation(2, 3);
	
	@SafeVarargs
	private static <T> Future<Duration> streamVerification(Flux<T> stream, T ... expectedItems){
		ExecutorService verifierExec = Executors.newSingleThreadExecutor();
		Future<Duration> blockVerification = verifierExec.submit(new Callable<Duration>() {
			@Override
			public Duration call() throws Exception {
				return StepVerifier.create(stream)
				.expectNext(expectedItems)
				.verifyComplete();
			}
		});
		return blockVerification;
	}
	
	private static Flux<String> getBlockIdsStream(TestTreeContext context){
		return context.strandExecutor.getBlockStream().map(Block::id);
	}
	
	@Test
	public void ignoreSingleSelectedLeaf() throws InterruptedException, ExecutionException {
		
		TestTreeContext context = TestTreeContext.builder(defaultRepresentation).ignore("0.2.2").build();

		Future<Duration> blockVerification = streamVerification(getBlockIdsStream(context), 
				"0", "0.0", "0.0.0", "0.0.1","0.0.2", "0.1", "0.1.0", "0.1.1", "0.1.2",
				"0.2", "0.2.0", "0.2.1");
		
		resumeAndWaitForRootStrandComplete(context);
		
		Map<String, RunState> runStateSnapshot = context.nodeStates.getRunStates().getSnapshot();
		Assertions.assertThat(runStateSnapshot).containsEntry("0.2.2", RunState.NOT_STARTED);
		Map<String, RunState> expectedRunStates = allFinishedBut(context, RunState.NOT_STARTED, "0.2.2");
		Assertions.assertThat(runStateSnapshot).containsAllEntriesOf(expectedRunStates);
		blockVerification.get();		
	}
	
	@Test
	public void ignoreSelectedBranchesAndTheirChildren() throws InterruptedException, ExecutionException {
		TestTreeContext context = TestTreeContext.builder(defaultRepresentation).ignore("0.0", "0.2").build();
		Future<Duration> blockVerification = streamVerification(getBlockIdsStream(context), 
				"0", "0.1", "0.1.0", "0.1.1","0.1.2");
		resumeAndWaitForRootStrandComplete(context);
		Map<String, RunState> runStateSnapshot = context.nodeStates.getRunStates().getSnapshot();
		Assertions.assertThat(runStateSnapshot).containsEntry("0.0", RunState.NOT_STARTED);
		Map<String, RunState> expectedRunStates = allFinishedBut(context, RunState.NOT_STARTED, "0.0",
				"0.0.0", "0.0.1", "0.0.2", "0.2", "0.2.0", "0.2.1", "0.2.2");
		Assertions.assertThat(runStateSnapshot).containsAllEntriesOf(expectedRunStates);
		blockVerification.get();
	}
	
	@Test
	public void ignoreAllFirstLevelBranches() throws InterruptedException, ExecutionException {
		TestTreeContext context = TestTreeContext.builder(defaultRepresentation).ignore("0.0", "0.1", "0.2").build();
		Future<Duration> verifier = streamVerification(context.strandExecutor.getBlockStream().map(Block::id), "0");
		resumeAndWaitForRootStrandComplete(context);
		verifier.get();
	}
	
	private void resumeAndWaitForRootStrandComplete(TestTreeContext context) {
		context.strandExecutor.instruct(StrandCommand.RESUME);
		context.strandExecutor.getStateStream().blockLast();
	}
	
	private Map<String, RunState> allFinishedBut(TestTreeContext testConfig, RunState alternativeState, String ... blockIds){
		Map<String, RunState> mapBuilder = new HashMap<>();
		for (int i = 0; i < blockIds.length; i++) {
			mapBuilder.put(blockIds[i], alternativeState);
		}
		testConfig.treeStructure.allBlocks().forEach(block -> {
			if(!mapBuilder.containsKey(block.id())) {
				mapBuilder.put(block.id(), RunState.FINISHED);	
			}
		});
		System.out.println(mapBuilder);
		return ImmutableMap.copyOf(mapBuilder);		
	}

}
