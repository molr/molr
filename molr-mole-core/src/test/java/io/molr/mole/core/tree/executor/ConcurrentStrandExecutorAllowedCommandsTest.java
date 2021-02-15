package io.molr.mole.core.tree.executor;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;
import reactor.core.publisher.Flux;

public class ConcurrentStrandExecutorAllowedCommandsTest {

    @Test
    public void testPausedLeafCommands() {
    	TestTreeContext context = TestTreeContext.builder(TestMissions.testRepresentation(2, 3))
    			.breakPoints("0.0.0").build();
    	Flux<String> blocks = context.strandExecutor().getBlockStream().map(Block::id).takeUntil(block->block.equals("0.0.0"));
    	context.strandExecutor().instruct(StrandCommand.RESUME);
    	blocks.blockLast();
    	context.strandExecutor().getStateStream().takeUntil(state ->state.equals(RunState.PAUSED)).blockLast();
    	Assertions.assertThat(context.strandExecutor().getAllowedCommands()).containsExactly(
    			StrandCommand.RESUME, StrandCommand.SKIP, StrandCommand.STEP_OVER);
    }
    
    @Test
    public void testRunninLeafCommands() {
    	TestTreeContext context = TestTreeContext.builder(TestMissions.testRepresentation(2, 3))
    			.latched("0.0.0").build();
    	context.strandExecutor().instruct(StrandCommand.RESUME);
    	/*
    	 * TODO the await command might be too late if strand executor is too fast
    	 */
    	try {
			context.entryLatches.get("0.0.0").await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("assert");
    	Assertions.assertThat(context.strandExecutor().getAllowedCommands()).containsExactly(
    			StrandCommand.PAUSE);
    }
    
    @Test
    public void testPausedSequentialBranch() {
    	TestTreeContext context = TestTreeContext.builder(TestMissions.testRepresentation(2, 3))
    			.breakPoints("0.0").build();
    	Flux<String> blocks = context.strandExecutor().getBlockStream().map(Block::id).takeUntil(block->block.equals("0.0"));
    	context.strandExecutor().instruct(StrandCommand.RESUME);
    	blocks.blockLast();
    	context.strandExecutor().getStateStream().takeUntil(state ->state.equals(RunState.PAUSED)).blockLast();
    	Assertions.assertThat(context.strandExecutor().getAllowedCommands()).containsExactlyInAnyOrder(
    			StrandCommand.RESUME, StrandCommand.SKIP, StrandCommand.STEP_OVER, StrandCommand.STEP_INTO);   	
    }
    
    /*
     * TODO not test paused but children executed? Possible to pause running?
     * Open question
     */
    @Test
    public void testPausedParallelBranch() {
    	TestTreeContext context = TestTreeContext.builder(TestMissions.testRepresentation(2, 3))
    			.breakPoints("0.0").parallel("0.0").build();
    	Flux<String> blocks = context.strandExecutor().getBlockStream().map(Block::id).takeUntil(block->block.equals("0.0"));
    	context.strandExecutor().instruct(StrandCommand.RESUME);
    	blocks.blockLast();
    	context.strandExecutor().getStateStream().takeUntil(state ->state.equals(RunState.PAUSED)).blockLast();
    	Assertions.assertThat(context.strandExecutor().getAllowedCommands()).containsExactlyInAnyOrder(
    			StrandCommand.RESUME, StrandCommand.SKIP, StrandCommand.STEP_OVER, StrandCommand.STEP_INTO);   	
    }
    
    @Test
    public void testRunningParallelBranch() throws InterruptedException {
    	TestTreeContext context = TestTreeContext.builder(TestMissions.testRepresentation(2, 3))
    			.parallel("0.0").latched("0.0.0", "0.0.1").build();
    	context.strandExecutor().instruct(StrandCommand.RESUME);
    	context.entryLatches.get("0.0.0").await();
    	context.entryLatches.get("0.0.1").await();
    	Assertions.assertThat(context.strandExecutor().getAllowedCommands()).containsExactlyInAnyOrder(
    			StrandCommand.PAUSE);       	
    }
	
}
