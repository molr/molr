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

@SuppressWarnings("unused")
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
        instructRootStrandSync(StrandCommand.RESUME);

        waitUntilRootStrandIsFinished();
        assertThatRootStrandState().isEqualTo(RunState.FINISHED);
        assertThatResultOf(treeStructure().rootBlock()).isEqualTo(Result.SUCCESS);
    }

    @Test
    public void testStepInto() {
        instructRootStrandSync(StrandCommand.STEP_INTO);
        waitUntilRootStrandBlockIs(FIRST);
        assertThatRootStrandBlock().isEqualTo(FIRST);

        instructRootStrandSync(StrandCommand.STEP_INTO);
        waitUntilRootStrandBlockIs(FIRST_A);
        assertThatRootStrandBlock().isEqualTo(FIRST_A);

        instructRootStrandSync(StrandCommand.STEP_INTO);
        waitUntilRootStrandBlockIs(FIRST_A);
        assertThatRootStrandBlock().isEqualTo(FIRST_A).as("Stepping into a leaf should have no effect");
    }

    @Test
    public void testStepOver() {
        moveRootStrandTo(FIRST_A);
        waitUntilRootStrandBlockIs(FIRST_A);

        instructRootStrandSync(StrandCommand.STEP_OVER);
        waitUntilResultOfBlockIs(FIRST_A, Result.SUCCESS);
        waitUntilRootStrandBlockIs(FIRST_B);
        assertThatRootStrandBlock().isEqualTo(FIRST_B).as("Strand should have moved to next after STEP_OVER success");

        instructRootStrandSync(StrandCommand.STEP_OVER);
        waitUntilResultOfBlockIs(FIRST_B, Result.SUCCESS);
        waitUntilRootStrandBlockIs(SECOND);
        assertThatRootStrandBlock().isEqualTo(SECOND).as("Strand should have moved to next after STEP_OVER success");

        List<Block> successfulBlocks = Arrays.asList(FIRST_A, FIRST_B, FIRST);
        treeStructure().allBlocks().forEach(block -> {
            if (successfulBlocks.contains(block)) {
                assertThatResultOf(block).isEqualTo(Result.SUCCESS).as("Result for %s should be SUCCESS!", block);
            } else {
                assertThatResultOf(block).isEqualTo(Result.UNDEFINED).as("Block %s should have not been evaluated in this test...", block);
            }
        });
    }

    @Test
    public void testSkippingLastBlockFinishes() {
        moveRootStrandTo(FOURTH);
        waitUntilRootStrandBlockIs(FOURTH);

        instructRootStrandSync(StrandCommand.SKIP);
        waitUntilRootStrandStateIs(RunState.FINISHED);
        assertThatRootStrandState().isEqualTo(RunState.FINISHED).as("Skipping the last block should finish the strand");
    }

    @Test
    public void testSkippingBlocks() {
        moveRootStrandTo(FIRST);
        waitUntilRootStrandBlockIs(FIRST);

        instructRootStrandSync(StrandCommand.SKIP);
        waitUntilRootStrandBlockIs(SECOND);

        instructRootStrandSync(StrandCommand.SKIP);
        waitUntilRootStrandBlockIs(THIRD);

        instructRootStrandSync(StrandCommand.SKIP);
        waitUntilRootStrandBlockIs(PARALLEL);

        instructRootStrandSync(StrandCommand.SKIP);
        waitUntilRootStrandBlockIs(FOURTH);

        instructRootStrandSync(StrandCommand.SKIP);
        waitUntilRootStrandStateIs(RunState.FINISHED);

        for (Block block : treeStructure().allBlocks()) {
            assertThatResultOf(block).isEqualTo(Result.UNDEFINED).as("Result should be UNDEFINED when skipping all blocks");
        }
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }

}
