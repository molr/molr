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

public class ConcurrentStrandExecutorChildrenExecutionTest extends AbstractSingleMissionStrandExecutorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentStrandExecutorStepOverParallelTest.class);

    private Block parallelBlock;
    private Block lastBlock;

    private CountDownLatch latchA1Start;
    private CountDownLatch latchB1Start;
    private CountDownLatch latchA1End;
    private CountDownLatch latchB1End;

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
                            bA.run("A.2", () -> {
                            });
                        });
                        b.sequential("sequential branch B", bB -> {
                            bB.run("B.1", () -> {
                                unlatch(latchB1Start);
                                await(latchB1End);
                            });
                            bB.run("B.2", () -> {
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
        latchA1End = new CountDownLatch(1);
        latchB1End = new CountDownLatch(1);
    }

    @Test
    public void testChildrenFinishWhileParentIsPauseShouldFinishParent() {
        moveRootStrandTo(parallelBlock);
        instructSync(StrandCommand.RESUME);

        await(latchA1Start, latchB1Start);
        instructSync(StrandCommand.PAUSE);
        unlatch(latchA1End, latchB1End);

        waitForStateToBe(PAUSED);
        childrenStrandExecutors().forEach(se -> waitForStrandStateToBe(se, PAUSED));

        assertThatActualState().isEqualTo(PAUSED);
        childrenStrandExecutors().forEach(se -> assertThatActualStateOf(se).isEqualTo(PAUSED));

        childrenStrandExecutors().forEach(se -> se.instruct(StrandCommand.RESUME));
        childrenStrandExecutors().forEach(this::waitForStrandToFinish);

        waitForStateToBe(FINISHED);
        assertThatActualState().isEqualTo(FINISHED);
    }

    @Test
    public void testChildrenFinishWhileParentIsPauseShouldMoveNextOnParent() {
        moveRootStrandTo(parallelBlock);
        instructSync(StrandCommand.STEP_OVER);

        await(latchA1Start, latchB1Start);
        instructSync(StrandCommand.PAUSE);
        unlatch(latchA1End, latchB1End);

        waitForStateToBe(PAUSED);
        childrenStrandExecutors().forEach(se -> waitForStrandStateToBe(se, PAUSED));

        assertThatActualState().isEqualTo(PAUSED);
        childrenStrandExecutors().forEach(se -> assertThatActualStateOf(se).isEqualTo(PAUSED));

        childrenStrandExecutors().forEach(se -> se.instruct(StrandCommand.RESUME));
        childrenStrandExecutors().forEach(this::waitForStrandToFinish);

        waitForActualBlockToBe(lastBlock);
        assertThatActualBlock().isEqualTo(lastBlock);
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }
}
