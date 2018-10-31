package org.molr.mole.core.tree;

import org.junit.After;
import org.junit.Test;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.Result;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.molr.mole.core.tree.support.AbstractSingleMissionStrandExecutorTest;
import org.molr.mole.core.utils.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcurrentStrandExecutorTest extends AbstractSingleMissionStrandExecutorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentStrandExecutorTest.class);

    private Block FIRST;
    private Block FIRST_A;
    private Block FIRST_B;
    private Block SECOND;
    private Block SECOND_A;
    private Block SECOND_B;
    private Block THIRD;
    private Block PARALLEL;
    private Block PARALLEL_A;
    private Block PARALLEL_B;
    private Block FOURTH;
    private Block FOURTH_A;

    @Override
    protected RunnableLeafsMission mission() {
        return new RunnableMissionSupport() {
            {
                mission("Root", root -> {
                    FIRST = root.sequential("First", b -> {
                        FIRST_A = b.run(log("First A"));
                        FIRST_B = b.run(log("First B"));
                    });

                    SECOND = root.sequential("Second", b -> {
                        SECOND_A = b.run(log("second A"));
                        SECOND_B = b.run(log("second B"));
                    });

                    THIRD = root.run(log("Third"));

                    PARALLEL = root.parallel("Parallel", b -> {
                        PARALLEL_A = b.run(log("parallel A"));
                        PARALLEL_B = b.run(log("parallel B"));
                    });

                    FOURTH = root.sequential("Fourth", b -> {
                        FOURTH_A = b.run(log("Fourth A"));
                    });
                });
            }
        }.build();
    }

    @After
    public void tearDown() {
        TreeUtils.logResultsOf(treeResultTracker(), treeStructure());
    }

    @Test
    public void testSuccess() {
        strandExecutor().instruct(StrandCommand.RESUME);

        assertThat(isFinishedSync(strandExecutor())).isTrue();
        assertThat(currentRootResult()).isEqualTo(Result.SUCCESS);
    }

    @Test
    public void testStepInto() {
        strandExecutor().instruct(StrandCommand.STEP_INTO);
        waitForActualBlockToBe(strandExecutor(), FIRST);
        assertThat(strandExecutor().getActualBlock()).isEqualTo(FIRST);

        strandExecutor().instruct(StrandCommand.STEP_INTO);
        waitForActualBlockToBe(strandExecutor(), FIRST_A);
        assertThat(strandExecutor().getActualBlock()).isEqualTo(FIRST_A);

        strandExecutor().instruct(StrandCommand.STEP_INTO);
        waitForActualBlockToBe(strandExecutor(), FIRST_A);
        assertThat(strandExecutor().getActualBlock()).isEqualTo(FIRST_A)
                .describedAs("Stepping into a leaf should have no effect");
    }

    @Test
    public void testStepOver() {
        moveTo(strandExecutor(), FIRST_A);
        waitForActualBlockToBe(strandExecutor(), FIRST_A);

        strandExecutor().instruct(StrandCommand.STEP_OVER);
        waitForResultOfBlockToBe(treeResultTracker(), FIRST_A, Result.SUCCESS);
        waitForActualBlockToBe(strandExecutor(), FIRST_B);
        assertThat(strandExecutor().getActualBlock()).isEqualTo(FIRST_B)
                .as("Strand should have moved to next after STEP_OVER success");

        strandExecutor().instruct(StrandCommand.STEP_OVER);
        waitForResultOfBlockToBe(treeResultTracker(), FIRST_B, Result.SUCCESS);
        waitForActualBlockToBe(strandExecutor(), SECOND);
        assertThat(strandExecutor().getActualBlock()).isEqualTo(SECOND)
                .as("Strand should have moved to next after STEP_OVER success");

        List<Block> successfulBlocks = Arrays.asList(FIRST_A, FIRST_B, FIRST);
        treeStructure().allBlocks().forEach(block -> {
            if (successfulBlocks.contains(block)) {
                assertThat(treeResultTracker().resultFor(block)).isEqualTo(Result.SUCCESS)
                        .as("Result for %s should be SUCCESS!", block);
            } else {
                assertThat(treeResultTracker().resultFor(block)).isEqualTo(Result.UNDEFINED)
                        .as("Block %s should have not been evaluated in this test...", block);
            }
        });
    }

    @Test
    public void testSkippingLastBlockFinishes() {
        moveTo(strandExecutor(), FOURTH);
        waitForActualBlockToBe(strandExecutor(), FOURTH);

        strandExecutor().instruct(StrandCommand.SKIP);
        waitForStateToBe(strandExecutor(), RunState.FINISHED);
        assertThat(strandExecutor().getActualState()).isEqualTo(RunState.FINISHED)
                .as("Skipping the last block should finish the strand");
    }

    @Test
    public void testSkippingBlocks() {
        moveTo(strandExecutor(), FIRST);
        waitForActualBlockToBe(strandExecutor(), FIRST);

        strandExecutor().instruct(StrandCommand.SKIP);
        waitForActualBlockToBe(strandExecutor(), SECOND);

        strandExecutor().instruct(StrandCommand.SKIP);
        waitForActualBlockToBe(strandExecutor(), THIRD);

        strandExecutor().instruct(StrandCommand.SKIP);
        waitForActualBlockToBe(strandExecutor(), PARALLEL);

        strandExecutor().instruct(StrandCommand.SKIP);
        waitForActualBlockToBe(strandExecutor(), FOURTH);

        strandExecutor().instruct(StrandCommand.SKIP);
        waitForStateToBe(strandExecutor(), RunState.FINISHED);

        for (Block block : treeStructure().allBlocks()) {
            assertThat(treeResultTracker().resultFor(block)).isEqualTo(Result.UNDEFINED)
                    .as("Result should be UNDEFINED when skipping all blocks");
        }
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }

}
