package io.molr.mole.core.tree.executor;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.molr.commons.domain.StrandCommand;

public class ConcurrentStrandExecutorInstructTests {
	
	@Test
	public void instruct_whileAnotherCommandIsEnqueued_runtimeExceptionIsThrown() throws InterruptedException {
		TestTreeContext context = TestTreeContext.builder(TestMissions.testRepresentation(2, 3)).build();
		boolean catchedRuntimeException = false;
		try {
			context.strandExecutor().instruct(StrandCommand.PAUSE);
		}
		catch(RuntimeException e) {
			catchedRuntimeException = true;
		}
		Assertions.assertThat(catchedRuntimeException).isTrue();
	}

}
