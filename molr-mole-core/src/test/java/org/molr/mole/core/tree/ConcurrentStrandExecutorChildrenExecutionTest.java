package org.molr.mole.core.tree;

import org.junit.Before;
import org.junit.Test;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.molr.mole.core.tree.support.AbstractSingleMissionStrandExecutorTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static org.molr.commons.domain.RunState.FINISHED;
import static org.molr.commons.domain.RunState.PAUSED;
import static org.molr.commons.domain.RunState.RUNNING;
import static org.molr.commons.domain.StrandCommand.PAUSE;
import static org.molr.commons.domain.StrandCommand.RESUME;

public class ConcurrentStrandExecutorChildrenExecutionTest extends AbstractSingleMissionStrandExecutorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentStrandExecutorStepOverParallelTest.class);

    private Block parallelBlock;
    private Block lastBlock;
    private Block blockA2;
    private Block blockB2;

    private CountDownLatch latchA1Start;
    private CountDownLatch latchB1Start;
    private CountDownLatch latchB2Start;
    private CountDownLatch latchA1End;
    private CountDownLatch latchB1End;
    private CountDownLatch latchB2End;

    @Override
    protected RunnableLeafsMission mission() {
        return new RunnableMissionSupport() {
            {
                mission("step-over", root -> {
                    parallelBlock = root.parallel("parallel", b -> {
                        b.sequential("sequential branch A", bA -> {
                            bA.run("A.1", () -> {
                                unlatch(latchA1Start);
                                await(latchA1End);
                            });
                            blockA2 = bA.run("A.2", () -> {
                            });
                        });
                        b.sequential("sequential branch B", bB -> {
                            bB.run("B.1", () -> {
                                unlatch(latchB1Start);
                                await(latchB1End);
                            });
                            blockB2 = bB.run("B.2", () -> {
                                unlatch(latchB2Start);
                                await(latchB2End);
                            });
                        });
                    });
                    lastBlock = root.run(log("After"));
                });
            }
        }.build();
    }

    @Before
    public void setUp() {
        latchA1Start = new CountDownLatch(1);
        latchB1Start = new CountDownLatch(1);
        latchB2Start = new CountDownLatch(1);
        latchA1End = new CountDownLatch(1);
        latchB1End = new CountDownLatch(1);
        latchB2End = new CountDownLatch(1);
    }

    @Test
    public void testChildrenFinishWhileParentIsPauseShouldFinishParent() {
        moveRootStrandTo(parallelBlock);
        instructRootSync(StrandCommand.RESUME);

        await(latchA1Start, latchB1Start);
        instructRootSync(PAUSE);
        unlatch(latchA1End, latchB1End, latchB2End);

        waitForRootStateToBe(PAUSED);
        rootChildrenStrandExecutors().forEach(se -> waitForStrandStateToBe(se, PAUSED));

        assertThatRootState().isEqualTo(PAUSED);
        rootChildrenStrandExecutors().forEach(se -> assertThatActualStateOf(se).isEqualTo(PAUSED));

        rootChildrenStrandExecutors().forEach(se -> se.instruct(StrandCommand.RESUME));
        rootChildrenStrandExecutors().forEach(this::waitForStrandToFinish);

        waitForRootStateToBe(FINISHED);
        assertThatRootState().isEqualTo(FINISHED);
    }

    @Test
    public void testChildrenFinishWhileParentIsPauseShouldMoveNextOnParent() {
        moveRootStrandTo(parallelBlock);
        instructRootSync(StrandCommand.STEP_OVER);

        await(latchA1Start, latchB1Start);
        instructRootSync(PAUSE);
        unlatch(latchA1End, latchB1End, latchB2End);

        waitForRootStateToBe(PAUSED);
        rootChildrenStrandExecutors().forEach(se -> waitForStrandStateToBe(se, PAUSED));

        assertThatRootState().isEqualTo(PAUSED);
        rootChildrenStrandExecutors().forEach(se -> assertThatActualStateOf(se).isEqualTo(PAUSED));

        rootChildrenStrandExecutors().forEach(se -> se.instruct(StrandCommand.RESUME));
        rootChildrenStrandExecutors().forEach(this::waitForStrandToFinish);

        waitForRootBlockToBe(lastBlock);
        assertThatRootBlock().isEqualTo(lastBlock);
    }

    @Test
    public void testIfAllChildrenArePausedParentShouldPause() {
        moveRootStrandTo(parallelBlock);
        instructRootSync(StrandCommand.RESUME);

        await(latchA1Start, latchB1Start);
        rootChildrenStrandExecutors().forEach(se -> instructAsync(se, PAUSE));
        unlatch(latchA1End, latchB1End, latchB2End);

        rootChildrenStrandExecutors().forEach(se -> waitForStrandStateToBe(se, PAUSED));
        waitForRootStateToBe(PAUSED);

        assertThatRootState().isEqualTo(PAUSED);
        rootChildrenStrandExecutors().forEach(se -> assertThatActualStateOf(se).isEqualTo(PAUSED));
    }

    @Test
    public void testIfAtLeastOneChildrenIsRunningParentIsWaiting() {
        /* WAITING_FOR_CHILDREN == RUNNING RunState */
        moveRootStrandTo(parallelBlock);
        instructRootSync(StrandCommand.RESUME);

        await(latchA1Start, latchB1Start);
        instructRootSync(PAUSE);
        unlatch(latchA1End, latchB1End);

        waitForRootStateToBe(PAUSED);

        StrandExecutor strandA = rootChildrenStrandExecutors().stream()
                .filter(se -> se.getActualBlock().equals(blockA2)).findFirst().get();

        StrandExecutor strandB = rootChildrenStrandExecutors().stream()
                .filter(se -> se.getActualBlock().equals(blockB2)).findFirst().get();

        assertThatActualStateOf(strandA).isEqualTo(PAUSED);
        assertThatActualStateOf(strandB).isEqualTo(PAUSED);

        instructAsync(strandB, RESUME);
        await(latchB2Start);

        waitForRootStateToBe(RUNNING);

        assertThatActualStateOf(strandA).isEqualTo(PAUSED);
        assertThatActualStateOf(strandB).isEqualTo(RUNNING);
        assertThatRootState().isEqualTo(RUNNING);
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }
}
