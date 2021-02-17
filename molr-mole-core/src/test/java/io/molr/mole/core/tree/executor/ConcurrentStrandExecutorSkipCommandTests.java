package io.molr.mole.core.tree.executor;

import org.junit.Test;

import io.molr.commons.domain.StrandCommand;

public class ConcurrentStrandExecutorSkipCommandTests {

	@Test
	public void skip() {
		TestTreeContext context = TestTreeContext.builder(TestMissions.testRepresentation(2, 3))
				.parallel("0.0").build();
		context.strandExecutor.getLastCommandStream().subscribe(System.out::println);
		for (int i = 0; i < 100; i++) {
			boolean instructed = false;
			while(!instructed) {
				try {
					//System.out.println("instruct");
					context.strandExecutor().instruct(StrandCommand.STEP_INTO);
					instructed = true;
				}
				catch(Exception e) {
					
				}
			}
		}
//		context.strandExecutor().instruct(StrandCommand.STEP_INTO);
//		context.strandExecutor().instruct(StrandCommand.STEP_INTO);
		sk.util.Threads.sleep(10000);
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
