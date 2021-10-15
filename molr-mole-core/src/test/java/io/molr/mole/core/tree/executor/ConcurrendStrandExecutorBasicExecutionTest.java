package io.molr.mole.core.tree.executor;

import java.time.Duration;

import org.junit.Test;

import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.testing.strand.StrandExecutorTestSupport;

@SuppressWarnings("static-method")
public class ConcurrendStrandExecutorBasicExecutionTest {

	
	
	@Test
	public void runThroughSimpleTree() {
		TestTreeContext context = TestTreeContext.builder(TestMissions.testRepresentation(2, 3)).build();
		context.resumeRoot();
		context.strandExecutor().getStateStream().blockLast(Duration.ofSeconds(30));
	}
	
	@Test
	public void test2() {
		TestTreeContext context = TestTreeContext.builder(TestMissions.testRepresentation(2, 3))
				.parallel("0.0").latched("0.0.0", "0.0.1", "0.0.2").build();
		context.resumeRoot();
		context.awaitEntry("0.0.0");
		context.awaitEntry("0.0.1");
		context.awaitEntry("0.0.2");
		
		long command = context.strandExecutor().instruct(StrandCommand.PAUSE);
		StrandExecutorTestSupport.waitForProcessedCommand(context.strandExecutor(), command);
		//context.strandExecutorFactory.getStrandExecutorFor(Strand.ofId("1")).instruct(StrandCommand.PAUSE);
		
		context.unlatch("0.0.0");
		context.unlatch("0.0.1");
		context.unlatch("0.0.2");
	
		
	}
	

}
