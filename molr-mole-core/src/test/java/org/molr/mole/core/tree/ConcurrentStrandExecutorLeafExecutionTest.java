package org.molr.mole.core.tree;

import org.junit.Test;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.molr.testing.strand.AbstractSingleMissionStrandExecutorTest;
import org.slf4j.Logger;

import static org.molr.commons.domain.Result.FAILED;
import static org.molr.commons.domain.Result.UNDEFINED;

public class ConcurrentStrandExecutorLeafExecutionTest extends AbstractSingleMissionStrandExecutorTest {

    private Block failingBlock;
    private Block anotherBlock;

    @Override
    protected RunnableLeafsMission mission() {
        return new RunnableMissionSupport() {
            {
                mission("test", root -> {
                    failingBlock = root.run("failing", () -> {
                        throw new RuntimeException("test");
                    });
                    anotherBlock = root.run(log("another task"));
                });
            }
        }.build();
    }

    @Test
    public void testFailingLeafPausesStrand() {
        instructRootStrandSync(StrandCommand.RESUME);

        waitUntilRootStrandStateIs(RunState.PAUSED);

        assertThatRootStrandBlock().isEqualTo(failingBlock);
        assertThatResultOf(failingBlock).isEqualTo(FAILED);
        assertThatResultOf(anotherBlock).isEqualTo(UNDEFINED);
        assertThatRootResult().as("root result should be failed even if there are still undefined nodes because the result of the node will not affect it")
                .isEqualTo(FAILED);
    }

    @Override
    public Logger logger() {
        return null;
    }
}
