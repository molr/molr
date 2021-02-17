package io.molr.mole.core.tree.executor;

import org.junit.Test;

public class ConcurrendStrandExecutorBasicExecutionTest {
	
	@Test
	public void test() {
		TestTreeContext context = TestTreeContext.builder(TestMissions.testRepresentation(2, 3))
				.parallel("0.0").build();
		context.resumeRoot();
		context.strandExecutor.getStateStream().blockLast();
	}

}
