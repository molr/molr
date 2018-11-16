package org.molr.mole.core.tree;

import org.junit.Before;
import org.junit.Test;
import org.molr.commons.domain.Block;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.molr.testing.strand.AbstractSingleMissionStrandExecutorTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static org.molr.commons.domain.RunState.PAUSED;
import static org.molr.commons.domain.RunState.RUNNING;
import static org.molr.commons.domain.StrandCommand.PAUSE;
import static org.molr.commons.domain.StrandCommand.RESUME;
import static org.molr.commons.domain.StrandCommand.SKIP;
import static org.molr.commons.domain.StrandCommand.STEP_INTO;
import static org.molr.commons.domain.StrandCommand.STEP_OVER;

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
        return new RunnableMissionSupport() {
            {
                mission("root", root -> {
                    leafBlock = root.run("leaf", () -> {
                        unlatch(latchLeafStart);
                        await(latchLeafEnd);
                    });

                    sequentialBlock = root.sequential("sequential-block", seq -> {
                        seq.run("sequence-leaf", () -> {});
                    });

                    parallelBlock = root.parallel("parallel", b -> {
                        b.sequential("sequential branch A", bA -> {
                            bA.run("A.1", () -> {
                                unlatch(latchA1Start);
                                await(latchA1End);
                                System.out.println();
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
        latchLeafStart = new CountDownLatch(1);
        latchLeafEnd = new CountDownLatch(1);
    }

    @Test
    public void testPausedLeafCommands() {
        moveRootStrandTo(leafBlock);
        assertThatRootStrandState().isEqualTo(PAUSED);

        assertThatStrandRootAllowedCommands().containsExactlyInAnyOrder(SKIP, RESUME, STEP_OVER);
    }

    @Test
    public void testRunningLeafCommands() {
        moveRootStrandTo(leafBlock);
        instructRootStrandAsync(RESUME);
        await(latchLeafStart);

        assertThatRootStrandState().isEqualTo(RUNNING);
        assertThatStrandRootAllowedCommands().containsExactlyInAnyOrder(PAUSE);
    }

    @Test
    public void testPausedSequentialBlockCommands() {
        moveRootStrandTo(sequentialBlock);
        assertThatRootStrandState().isEqualTo(PAUSED);
        assertThatStrandRootAllowedCommands().containsExactlyInAnyOrder(SKIP, RESUME, STEP_OVER, STEP_INTO);
    }

    @Test
    public void testPausedParallelBlockCommands() {
        moveRootStrandTo(parallelBlock);
        assertThatRootStrandState().isEqualTo(PAUSED);
        assertThatStrandRootAllowedCommands().containsExactlyInAnyOrder(SKIP, RESUME, STEP_OVER, STEP_INTO);
    }

    @Test
    public void testWaitingForChildrenParallelBlockCommands() {
        moveRootStrandTo(parallelBlock);
        instructRootStrandSync(STEP_OVER);

        await(latchA1Start, latchB1Start);
        assertThatRootStrandState().isEqualTo(RUNNING);

        assertThatStrandRootAllowedCommands().containsExactlyInAnyOrder(PAUSE);
    }

    @Test
    public void testPausedParallelBlockWithChildrenCommands() {
        moveRootStrandTo(parallelBlock);
        instructRootStrandSync(STEP_OVER);

        await(latchA1Start, latchB1Start);
        instructRootStrandSync(PAUSE);
        unlatch(latchA1End, latchB1End);

        waitUntilRootStrandStateIs(PAUSED);

        assertThatStrandRootAllowedCommands().containsExactlyInAnyOrder(RESUME);
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }

}
