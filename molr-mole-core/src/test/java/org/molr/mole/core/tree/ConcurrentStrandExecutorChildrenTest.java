package org.molr.mole.core.tree;

import org.junit.Before;
import org.junit.Test;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import org.molr.mole.core.tree.exception.RejectedCommandException;
import org.molr.testing.strand.AbstractSingleMissionStrandExecutorTest;
import org.molr.testing.strand.StrandErrorsRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static org.molr.commons.domain.RunState.*;
import static org.molr.commons.domain.StrandCommand.*;

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
        return new RunnableLeafsMissionSupport() {
            {
                sequential("Root", root -> {
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
        instructRootStrandSync(STEP_OVER);
        await(latchAStart, latchBStart);

        StrandErrorsRecorder recorder = recordRootStrandErrors();
        waitUntilRootStrandStateIs(RUNNING);

        instructRootStrandSync(StrandCommand.SKIP);

        waitForErrorOfType(recorder, RejectedCommandException.class);
        assertThat(recorder).allMatch(RejectedCommandException.class::isInstance);
    }

    @Test
    public void testSkipAfterResumeWithChildrenThrows() {
        StrandErrorsRecorder recorder = recordRootStrandErrors();

        moveRootStrandTo(blockParallel);
        instructRootStrandSync(RESUME);
        await(latchAStart, latchBStart);

        waitUntilRootStrandStateIs(RUNNING);

        instructRootStrandSync(StrandCommand.SKIP);

        waitForErrorOfType(recorder, RejectedCommandException.class);
        assertThat(recorder).allMatch(RejectedCommandException.class::isInstance);
    }

    @Test
    public void testStepIntoAfterStepOverWithChildrenThrows() {
        moveRootStrandTo(blockParallel);
        instructRootStrandSync(STEP_OVER);
        await(latchAStart, latchBStart);

        StrandErrorsRecorder recorder = recordRootStrandErrors();
        waitUntilRootStrandStateIs(RUNNING);

        instructRootStrandSync(StrandCommand.STEP_INTO);

        waitForErrorOfType(recorder, RejectedCommandException.class);
        assertThat(recorder).allMatch(RejectedCommandException.class::isInstance);
    }

    @Test
    public void testStepIntoAfterResumeWithChildrenThrows() {
        moveRootStrandTo(blockParallel);
        instructRootStrandSync(RESUME);
        await(latchAStart, latchBStart);

        StrandErrorsRecorder recorder = recordRootStrandErrors();
        waitUntilRootStrandStateIs(RUNNING);

        instructRootStrandSync(StrandCommand.STEP_INTO);

        waitForErrorOfType(recorder, RejectedCommandException.class);
        assertThat(recorder).allMatch(RejectedCommandException.class::isInstance);
    }

    @Test
    public void testStepOverAfterStepOverWithChildrenThrows() {
        moveRootStrandTo(blockParallel);
        instructRootStrandSync(STEP_OVER);
        await(latchAStart, latchBStart);

        StrandErrorsRecorder recorder = recordRootStrandErrors();
        waitUntilRootStrandStateIs(RUNNING);

        instructRootStrandSync(STEP_OVER);

        waitForErrorOfType(recorder, RejectedCommandException.class);
        assertThat(recorder).allMatch(RejectedCommandException.class::isInstance);
    }

    @Test
    public void testStepOverAfterResumeWithChildrenThrows() {
        moveRootStrandTo(blockParallel);
        instructRootStrandSync(RESUME);
        await(latchAStart, latchBStart);

        StrandErrorsRecorder recorder = recordRootStrandErrors();
        waitUntilRootStrandStateIs(RUNNING);

        instructRootStrandSync(STEP_OVER);

        waitForErrorOfType(recorder, RejectedCommandException.class);
        assertThat(recorder).allMatch(RejectedCommandException.class::isInstance);
    }

    @Test
    public void testResumeWithChildrenResumesTheChildren() {
        moveRootStrandTo(blockParallel);
        instructRootStrandSync(RESUME);
        await(latchAStart, latchBStart);

        waitUntilRootStrandStateIs(RUNNING);
        rootStrandChildren().forEach(se -> waitUntilStrandStateIs(se, RUNNING));

        instructRootStrandSync(PAUSE);
        unlatch(latchAEnd, latchBEnd);
        waitUntilRootStrandStateIs(PAUSED);

        rootStrandChildren().forEach(se -> waitUntilStrandStateIs(se, PAUSED));

        assertThatRootStrandState().isEqualTo(PAUSED);
        rootStrandChildren().forEach(se -> assertThatStateOf(se).isEqualTo(PAUSED));

        instructRootStrandSync(RESUME);

        waitUntilRootStrandStateIs(FINISHED);
        rootStrandChildren().forEach(se -> assertThatStateOf(se).isEqualTo(FINISHED));
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }

}
