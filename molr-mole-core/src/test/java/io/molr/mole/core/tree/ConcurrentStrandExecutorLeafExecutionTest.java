package io.molr.mole.core.tree;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import io.molr.mole.core.testing.strand.AbstractSingleMissionStrandExecutorTest;
import org.junit.Test;
import org.slf4j.Logger;

import static io.molr.commons.domain.Result.FAILED;
import static io.molr.commons.domain.Result.UNDEFINED;

public class ConcurrentStrandExecutorLeafExecutionTest extends AbstractSingleMissionStrandExecutorTest {

    private Block failingBlock;
    private Block anotherBlock;

    @Override
    protected RunnableLeafsMission mission() {
        return new RunnableLeafsMissionSupport() {
            {
                root("test").sequential().as(root -> {
                    root.leaf("failing").run(() -> {
                        throw new RuntimeException("test");
                    });
                    failingBlock = latestBlock();

                    log(root, "another task");
                    anotherBlock = latestBlock();
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
