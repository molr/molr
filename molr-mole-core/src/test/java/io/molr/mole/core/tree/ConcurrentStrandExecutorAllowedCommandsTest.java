package io.molr.mole.core.tree;

import io.molr.commons.domain.Block;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import io.molr.mole.core.testing.strand.AbstractSingleMissionStrandExecutorTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static io.molr.commons.domain.RunState.PAUSED;
import static io.molr.commons.domain.RunState.RUNNING;
import static io.molr.commons.domain.StrandCommand.*;

public class ConcurrentStrandExecutorAllowedCommandsTest extends AbstractSingleMissionStrandExecutorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentStrandExecutorStepOverParallelTest.class);

    private Block parallelBlock;
    private Block lastBlock;
    private Block blockA2;
    private Block blockB2;
    private Block leafBlock;
    private Block sequentialBlock;

    private CountDownLatch latchA1Start;
    private CountDownLatch latchB1Start;
    private CountDownLatch latchB2Start;
    private CountDownLatch latchA1End;
    private CountDownLatch latchB1End;
    private CountDownLatch latchB2End;
    private CountDownLatch latchLeafStart;
    private CountDownLatch latchLeafEnd;

    @Override
    protected RunnableLeafsMission mission() {
        return new RunnableLeafsMissionSupport() {
            {
                root("root").sequential().as(root -> {
                    root.leaf("leaf").run(() -> {
                        unlatch(latchLeafStart);
                        await(latchLeafEnd);
                    });
                    leafBlock = latestBlock();

                    root.branch("sequential-block").sequential().as(seq -> {
                        sequentialBlock = latestBlock();
                        seq.leaf("sequence-leaf").run(() -> {
                        });
                    });

                    root.branch("parallel").parallel().as(b -> {
                        parallelBlock = latestBlock();

                        b.branch("sequential branch A").sequential().as(bA -> {
                            bA.leaf("A.1").run(() -> {
                                unlatch(latchA1Start);
                                await(latchA1End);
                                System.out.println();
                            });
                            bA.leaf("A.2").run(() -> {
                            });
                            blockA2 = latestBlock();
                        });
                        b.branch("sequential branch B").sequential().as( bB -> {
                            bB.leaf("B.1").run(() -> {
                                unlatch(latchB1Start);
                                await(latchB1End);
                            });

                            bB.leaf("B.2").run(() -> {
                                unlatch(latchB2Start);
                                await(latchB2End);
                            });
                            blockB2 = latestBlock();
                        });
                    });
                    log(root, "After");
                    lastBlock = latestBlock();

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
        latchLeafStart = new CountDownLatch(1);
        latchLeafEnd = new CountDownLatch(1);
    }

//    @Test
//    public void testPausedLeafCommands() {
//        moveRootStrandTo(leafBlock);
//        assertThatRootStrandState().isEqualTo(PAUSED);
//
//        assertThatStrandRootAllowedCommands().containsExactlyInAnyOrder(SKIP, RESUME, STEP_OVER);
//    }
//
//    @Test
//    public void testRunningLeafCommands() {
//        moveRootStrandTo(leafBlock);
//        instructRootStrandAsync(RESUME);
//        await(latchLeafStart);
//
//        assertThatRootStrandState().isEqualTo(RUNNING);
//        assertThatStrandRootAllowedCommands().containsExactlyInAnyOrder(PAUSE);
//    }
//
//    @Test
//    public void testPausedSequentialBlockCommands() {
//        moveRootStrandTo(sequentialBlock);
//        assertThatRootStrandState().isEqualTo(PAUSED);
//        assertThatStrandRootAllowedCommands().containsExactlyInAnyOrder(SKIP, RESUME, STEP_OVER, STEP_INTO);
//    }
//
//    @Test
//    public void testPausedParallelBlockCommands() {
//        moveRootStrandTo(parallelBlock);
//        assertThatRootStrandState().isEqualTo(PAUSED);
//        assertThatStrandRootAllowedCommands().containsExactlyInAnyOrder(SKIP, RESUME, STEP_OVER, STEP_INTO);
//    }
//
//    @Test
//    public void testWaitingForChildrenParallelBlockCommands() {
//        moveRootStrandTo(parallelBlock);
//        instructRootStrandSync(STEP_OVER);
//
//        await(latchA1Start, latchB1Start);
//        waitUntilRootStrandStateIs(RUNNING);
//
//        assertThatStrandRootAllowedCommands().containsExactlyInAnyOrder(PAUSE);
//    }
//
//    @Test
//    public void testPausedParallelBlockWithChildrenCommands() {
//        moveRootStrandTo(parallelBlock);
//        instructRootStrandSync(STEP_OVER);
//
//        await(latchA1Start, latchB1Start);
//        instructRootStrandSync(PAUSE);
//        unlatch(latchA1End, latchB1End);
//
//        waitUntilRootStrandStateIs(PAUSED);
//
//        assertThatStrandRootAllowedCommands().containsExactlyInAnyOrder(RESUME);
//    }

    @Override
    public Logger logger() {
        return LOGGER;
    }

}
