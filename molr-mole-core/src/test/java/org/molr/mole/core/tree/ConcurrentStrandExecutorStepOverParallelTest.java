package org.molr.mole.core.tree;

import org.junit.Before;
import org.junit.Test;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.Result;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.molr.mole.core.tree.support.AbstractSingleMissionStrandExecutorTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class ConcurrentStrandExecutorStepOverParallelTest extends AbstractSingleMissionStrandExecutorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentStrandExecutorStepOverParallelTest.class);

    private Block parallel;
    private Block parallelA1;
    private Block parallelA2;
    private Block parallelB1;
    private Block parallelB2;

    private CountDownLatch latchA1Start;
    private CountDownLatch latchB1Start;
    private CountDownLatch latchA1End;
    private CountDownLatch latchB1End;
    private CountDownLatch latchA2Start;
    private CountDownLatch latchB2Start;
    private CountDownLatch latchA2End;
    private CountDownLatch latchB2End;

    @Override
    protected RunnableLeafsMission mission() {
        return new RunnableMissionSupport() {
            {
                mission("step-over", root -> {
                    parallel = root.parallel("parallel", b -> {
                        b.sequential("sequential branch A", bA -> {
                            parallelA1 = bA.run("A.1", () -> {
                                unlatch(latchA1Start);
                                await(latchA1End);
                            });
                            parallelA2 = bA.run("A.2", () -> {
                                unlatch(latchA2Start);
                                await(latchA2End);
                            });
                        });
                        b.sequential("sequential branch B", bB -> {
                            parallelB1 = bB.run("B.1", () -> {
                                unlatch(latchB1Start);
                                await(latchB1End);
                            });
                            parallelB2 = bB.run("B.2", () -> {
                                unlatch(latchB2Start);
                                await(latchB2End);
                            });
                        });
                    });
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
        latchA2Start = new CountDownLatch(1);
        latchB2Start = new CountDownLatch(1);
        latchA2End = new CountDownLatch(1);
        latchB2End = new CountDownLatch(1);
    }

    @Test
    public void testStepOverTwiceWithParallelStrands() {
        moveRootStrandTo(parallel);
        assertThatActualBlock().isEqualTo(parallel);

        instructSync(StrandCommand.STEP_OVER);
        await(latchA1Start, latchB1Start);

        instructSync(StrandCommand.STEP_OVER);

        unlatch(latchA1End, latchB1End, latchA2End, latchB2End);

        waitForStateToBe(RunState.FINISHED);
        assertThatResultOf(parallel).isEqualTo(Result.SUCCESS);
        assertThatResultOf(parallelA1).isEqualTo(Result.SUCCESS);
        assertThatResultOf(parallelA2).isEqualTo(Result.SUCCESS);
        assertThatResultOf(parallelB1).isEqualTo(Result.SUCCESS);
        assertThatResultOf(parallelB2).isEqualTo(Result.SUCCESS);
    }

    @Test
    public void testResumeAfterStepOverWithParallelStrands() {
        moveRootStrandTo(parallel);
        assertThatActualBlock().isEqualTo(parallel);

        instructSync(StrandCommand.STEP_OVER);
        await(latchA1Start, latchB1Start);

        instructSync(StrandCommand.RESUME);

        unlatch(latchA1End, latchB1End, latchA2End, latchB2End);

        waitForStateToBe(RunState.FINISHED);
        assertThatResultOf(parallel).isEqualTo(Result.SUCCESS);
        assertThatResultOf(parallelA1).isEqualTo(Result.SUCCESS);
        assertThatResultOf(parallelA2).isEqualTo(Result.SUCCESS);
        assertThatResultOf(parallelB1).isEqualTo(Result.SUCCESS);
        assertThatResultOf(parallelB2).isEqualTo(Result.SUCCESS);
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }
}
