package io.molr.mole.core.tree.executor;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import io.molr.mole.core.testing.strand.AbstractSingleMissionStrandExecutorTest;
import io.molr.mole.core.testing.strand.StrandErrorsRecorder;
import io.molr.mole.core.tree.exception.RejectedCommandException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static io.molr.commons.domain.RunState.*;
import static io.molr.commons.domain.StrandCommand.*;

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
                root("Root").sequential().as(root -> {
                    root.branch("Parallel").parallel().as(p -> {
                        blockParallel = latestBlock();

                        p.branch("Sequence A").sequential().as( seqA -> {
                            seqA.leaf("A.1").run(() -> {
                                unlatch(latchAStart);
                                await(latchAEnd);
                            });
                            blockA1 = latestBlock();

                            seqA.leaf("A.2").run(NOOP);
                            blockA2 = latestBlock();
                        });
                        p.branch("Sequence B").sequential().as( seqB -> {
                            seqB.leaf("B.1").run(() -> {
                                unlatch(latchBStart);
                                await(latchBEnd);
                            });
                            blockB1 = latestBlock();

                            seqB.leaf("B.2").run(NOOP);
                            blockB2 = latestBlock();
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
