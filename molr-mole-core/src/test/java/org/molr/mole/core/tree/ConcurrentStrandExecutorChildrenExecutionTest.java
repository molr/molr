package org.molr.mole.core.tree;

import org.junit.Before;
import org.junit.Ignore;
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

    private Block parallel;
    private Block parallelA1;
    private Block parallelA2;
    private Block parallelB1;
    private Block parallelB2;

    private CountDownLatch latchA1Start;
    private CountDownLatch latchB1Start;
    private CountDownLatch latchA1End;
    private CountDownLatch latchB1End;

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
                            });
                        });
                        b.sequential("sequential branch B", bB -> {
                            parallelB1 = bB.run("B.1", () -> {
                                unlatch(latchB1Start);
                                await(latchB1End);
                            });
                            parallelB2 = bB.run("B.2", () -> {
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
    }

    @Ignore
    @Test
    public void testChildrenFinishWhileParentIsPauseShouldFinishParent() {
        moveRootStrandTo(parallel);
        rootStrandExecutor().instruct(StrandCommand.RESUME);

        await(latchA1Start, latchB1Start);
        rootStrandExecutor().instruct(StrandCommand.PAUSE);
        waitForStateToBe(PAUSED);

        unlatch(latchA1End, latchB1End);
        childrenStrandExecutors().forEach(se -> waitForStrandStateToBe(se, PAUSED));

        assertThatActualState().isEqualTo(PAUSED);
        childrenStrandExecutors().forEach(se -> assertThatActualStateOf(se).isEqualTo(PAUSED));

        childrenStrandExecutors().forEach(se -> se.instruct(StrandCommand.RESUME));
        childrenStrandExecutors().forEach(this::waitForStrandToFinish);

        // TODO put wait for state finished on parent when test passes!
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThatActualState().isEqualTo(FINISHED);
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }
}
