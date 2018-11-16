package org.molr.mole.core.tree;

import org.junit.Test;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import org.molr.mole.core.tree.support.AbstractSingleMissionStrandExecutorTest;
import org.slf4j.Logger;

import static org.molr.commons.domain.Result.FAILED;
import static org.molr.commons.domain.Result.UNDEFINED;

public class ConcurrentStrandExecutorLeafExecutionTest extends AbstractSingleMissionStrandExecutorTest {

    private Block failingBlock;

    @Override
    protected RunnableLeafsMission mission() {
        return new RunnableLeafsMissionSupport() {
            {
                mission("test", root -> {
                    failingBlock = root.run("failing", () -> {
                        throw new RuntimeException("test");
                    });
                    root.run(log("another task"));
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
        assertThatRootResult().as("root result should not be calculated as there are other blocks in the sequence")
                .isEqualTo(UNDEFINED);
    }

    @Override
    public Logger logger() {
        return null;
    }
}
