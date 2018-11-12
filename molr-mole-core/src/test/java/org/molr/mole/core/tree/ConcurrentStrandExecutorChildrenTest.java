package org.molr.mole.core.tree;

import org.junit.Before;
import org.junit.Test;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.molr.mole.core.tree.exception.RejectedCommandException;
import org.molr.mole.core.tree.support.AbstractSingleMissionStrandExecutorTest;
import org.molr.mole.core.tree.support.StrandErrorsRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static org.molr.commons.domain.RunState.FINISHED;
import static org.molr.commons.domain.RunState.PAUSED;
import static org.molr.commons.domain.RunState.RUNNING;
import static org.molr.commons.domain.StrandCommand.PAUSE;
import static org.molr.commons.domain.StrandCommand.RESUME;
import static org.molr.commons.domain.StrandCommand.STEP_OVER;

@SuppressWarnings("unused")
public class ConcurrentStrandExecutorChildrenTest extends AbstractSingleMissionStrandExecutorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentStrandExecutorChildrenTest.class);
    private static final Runnable NOOP = () -> {
    };

    private Block blockParallel;
    private Block blockA1;
    private Block blockA2;
    private Block blockB1;
    private Block blockB2;

    private CountDownLatch latchAStart;
    private CountDownLatch latchAEnd;
    private CountDownLatch latchBStart;
    private CountDownLatch latchBEnd;

    @Override
    protected RunnableLeafsMission mission() {
        LOGGER.info("MISSION CREATED");
        return new RunnableMissionSupport() {
            {
                mission("Root", root -> {
                    blockParallel = root.parallel("Parallel", p -> {
                        p.sequential("Sequence A", seqA -> {
                            blockA1 = seqA.run("A.1", () -> {
                                unlatch(latchAStart);
                                await(latchAEnd);
                            });
                            blockA2 = seqA.run("A.2", NOOP);
                        });
                        p.sequential("Sequence B", seqB -> {
                            blockB1 = seqB.run("B.1", () -> {
                                unlatch(latchBStart);
                                await(latchBEnd);
                            });
                            blockB2 = seqB.run("B.2", NOOP);
                        });
                    });
                });
            }
        }.build();
    }

    @Before
    public void setUp() {
        latchAStart = new CountDownLatch(1);
        latchAEnd = new CountDownLatch(1);
        latchBStart = new CountDownLatch(1);
        latchBEnd = new CountDownLatch(1);
    }

    @Test
    public void testSkipAfterStepOverWithChildrenThrows() {
        moveRootStrandTo(blockParallel);
        instructSync(STEP_OVER);
        await(latchAStart, latchBStart);

        StrandErrorsRecorder recorder = recordStrandErrors();
        waitForStateToBe(RUNNING);
        assertThatActualState().isEqualTo(RUNNING);


        instructSync(StrandCommand.SKIP);

        waitForErrorOfType(recorder, RejectedCommandException.class);
        assertThat(recorder).allMatch(RejectedCommandException.class::isInstance);
    }

    @Test
    public void testSkipAfterResumeWithChildrenThrows() {
        StrandErrorsRecorder recorder = recordStrandErrors();

        moveRootStrandTo(blockParallel);
        instructSync(RESUME);
        await(latchAStart, latchBStart);

        waitForStateToBe(RUNNING);
        assertThatActualState().isEqualTo(RUNNING);

        instructSync(StrandCommand.SKIP);

        waitForErrorOfType(recorder, RejectedCommandException.class);
        assertThat(recorder).allMatch(RejectedCommandException.class::isInstance);
    }

    @Test
    public void testStepIntoAfterStepOverWithChildrenThrows() {
        moveRootStrandTo(blockParallel);
        instructSync(STEP_OVER);
        await(latchAStart, latchBStart);

        StrandErrorsRecorder recorder = recordStrandErrors();
        waitForStateToBe(RUNNING);
        assertThatActualState().isEqualTo(RUNNING);

        instructSync(StrandCommand.STEP_INTO);

        waitForErrorOfType(recorder, RejectedCommandException.class);
        assertThat(recorder).allMatch(RejectedCommandException.class::isInstance);
    }

    @Test
    public void testStepIntoAfterResumeWithChildrenThrows() {
        moveRootStrandTo(blockParallel);
        instructSync(RESUME);
        await(latchAStart, latchBStart);

        StrandErrorsRecorder recorder = recordStrandErrors();
        waitForStateToBe(RUNNING);
        assertThatActualState().isEqualTo(RUNNING);

        instructSync(StrandCommand.STEP_INTO);

        waitForErrorOfType(recorder, RejectedCommandException.class);
        assertThat(recorder).allMatch(RejectedCommandException.class::isInstance);
    }

    @Test
    public void testStepOverAfterStepOverWithChildrenThrows() {
        moveRootStrandTo(blockParallel);
        instructSync(STEP_OVER);
        await(latchAStart, latchBStart);

        StrandErrorsRecorder recorder = recordStrandErrors();
        waitForStateToBe(RUNNING);
        assertThatActualState().isEqualTo(RUNNING);

        instructSync(STEP_OVER);

        waitForErrorOfType(recorder, RejectedCommandException.class);
        assertThat(recorder).allMatch(RejectedCommandException.class::isInstance);
    }

    @Test
    public void testStepOverAfterResumeWithChildrenThrows() {
        moveRootStrandTo(blockParallel);
        instructSync(RESUME);
        await(latchAStart, latchBStart);

        StrandErrorsRecorder recorder = recordStrandErrors();
        waitForStateToBe(RUNNING);
        assertThatActualState().isEqualTo(RUNNING);

        instructSync(STEP_OVER);

        waitForErrorOfType(recorder, RejectedCommandException.class);
        assertThat(recorder).allMatch(RejectedCommandException.class::isInstance);
    }

    @Test
    public void testResumeWithChildrenResumesTheChildren() {
        moveRootStrandTo(blockParallel);
        instructSync(RESUME);
        await(latchAStart, latchBStart);

        waitForStateToBe(RUNNING);
        childrenStrandExecutors().forEach(se -> waitForStrandStateToBe(se, RUNNING));
        assertThatActualState().isEqualTo(RUNNING);
        childrenStrandExecutors().forEach(se -> assertThatActualStateOf(se).isEqualTo(RUNNING));

        instructSync(PAUSE);
        unlatch(latchAEnd, latchBEnd);
        waitForStateToBe(PAUSED);

        childrenStrandExecutors().forEach(se -> waitForStrandStateToBe(se, PAUSED));

        assertThatActualState().isEqualTo(PAUSED);
        childrenStrandExecutors().forEach(se -> assertThatActualStateOf(se).isEqualTo(PAUSED));

        instructSync(RESUME);

        waitForStateToBe(FINISHED);
        childrenStrandExecutors().forEach(se -> assertThatActualStateOf(se).isEqualTo(FINISHED));
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }

}
