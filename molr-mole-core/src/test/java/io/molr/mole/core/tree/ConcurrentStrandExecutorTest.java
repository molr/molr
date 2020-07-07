package io.molr.mole.core.tree;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import io.molr.mole.core.testing.strand.AbstractSingleMissionStrandExecutorTest;
import io.molr.mole.core.utils.Trees;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static io.molr.commons.domain.Result.SUCCESS;
import static io.molr.commons.domain.Result.UNDEFINED;
import static io.molr.commons.domain.RunState.FINISHED;
import static io.molr.commons.domain.RunState.PAUSED;

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
        return new RunnableLeafsMissionSupport() {
            {
                root("Root").sequential().as(root -> {
                    root.branch("First").sequential().as(b1 -> {
                        FIRST = latestBlock();

                        log(b1, "First A");
                        FIRST_A = latestBlock();

                        log(b1, "First B");
                        FIRST_B = latestBlock();
                    });

                    root.branch("Second").sequential().as(b1 -> {
                        SECOND = latestBlock();

                        log(b1, "second A");
                        SECOND_A = latestBlock();

                        log(b1, "second B");
                        SECOND_B = latestBlock();
                    });

                    log(root, "Third");
                    THIRD = latestBlock();

                    root.branch("Parallel").parallel().as(b1 -> {
                        PARALLEL = latestBlock();

                        log(b1, "parallel A");
                        PARALLEL_A = latestBlock();

                        log(b1, "parallel B");
                        PARALLEL_B = latestBlock();
                    });

                    root.branch("Fourth").sequential().as(b -> {
                        FOURTH = latestBlock();

                        log(b, "Fourth A");
                        FOURTH_A = latestBlock();
                    });
                });
            }
        }.build();
    }

    @After
    public void tearDown() {
        Trees.logResultsOf(treeResultTracker(), treeStructure());
    }

    @Test
    public void testSuccess() {
        instructRootStrandSync(StrandCommand.RESUME);

        waitUntilRootStrandIsFinished();
        assertThatResultOf(treeStructure().rootBlock()).isEqualTo(SUCCESS);
    }

    @Test
    public void testStepInto() {
        instructRootStrandSync(StrandCommand.STEP_INTO);
        waitUntilRootStrandBlockIs(FIRST);

        instructRootStrandSync(StrandCommand.STEP_INTO);
        waitUntilRootStrandBlockIs(FIRST_A);
    }

    @Test
    public void testStepIntoALeafHasNoEffect() {
        moveRootStrandTo(FIRST_A);
        instructRootStrandSync(StrandCommand.STEP_INTO);
        assertThatRootStrandBlock().as("Stepping into a leaf should have no effect").isEqualTo(FIRST_A);
    }

    @Test
    public void testStepIntoParallelBlockSpawnsStrands() {
        moveRootStrandTo(PARALLEL);
        instructRootStrandSync(StrandCommand.STEP_INTO);
        waitUntilRootStrandStateIs(PAUSED);
        rootStrandChildren().forEach(se -> waitUntilStrandStateIs(se, PAUSED));

        Assertions.assertThat(rootStrandChildren()).hasSize(2);
    }

    @Test
    public void testStepOver() {
        moveRootStrandTo(FIRST_A);
        waitUntilRootStrandBlockIs(FIRST_A);

        instructRootStrandSync(StrandCommand.STEP_OVER);
        waitUntilResultOfBlockIs(FIRST_A, SUCCESS);
        waitUntilRootStrandBlockIs(FIRST_B);

        instructRootStrandSync(StrandCommand.STEP_OVER);
        waitUntilResultOfBlockIs(FIRST_B, SUCCESS);
        waitUntilRootStrandBlockIs(SECOND);

        List<Block> successfulBlocks = Arrays.asList(FIRST_A, FIRST_B, FIRST);
        treeStructure().allBlocks().forEach(block -> {
            if (successfulBlocks.contains(block)) {
                assertThatResultOf(block).as("Result for %s should be SUCCESS!", block).isEqualTo(SUCCESS);
            } else {
                assertThatResultOf(block).as("Block %s should have not been evaluated in this test...", block).isEqualTo(UNDEFINED);
            }
        });
    }

    @Test
    public void testSkippingLastBlockFinishes() {
        moveRootStrandTo(FOURTH);
        waitUntilRootStrandBlockIs(FOURTH);

        instructRootStrandSync(StrandCommand.SKIP);
        assertThatRootStrandState().as("Skipping the last block should finish the strand").isEqualTo(FINISHED);
    }

    @Test
    public void testSkippingBlocks() {
        moveRootStrandTo(FIRST);

        instructRootStrandSync(StrandCommand.SKIP);
        waitUntilRootStrandBlockIs(SECOND);

        instructRootStrandSync(StrandCommand.SKIP);
        waitUntilRootStrandBlockIs(THIRD);

        instructRootStrandSync(StrandCommand.SKIP);
        waitUntilRootStrandBlockIs(PARALLEL);

        instructRootStrandSync(StrandCommand.SKIP);
        waitUntilRootStrandBlockIs(FOURTH);

        instructRootStrandSync(StrandCommand.SKIP);
        waitUntilRootStrandStateIs(FINISHED);

        for (Block block : treeStructure().allBlocks()) {
            assertThatResultOf(block).as("Result should be UNDEFINED when skipping all blocks").isEqualTo(UNDEFINED);
        }
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }

}

