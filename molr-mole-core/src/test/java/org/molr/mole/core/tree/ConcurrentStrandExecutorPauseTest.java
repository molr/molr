package org.molr.mole.core.tree;

import org.junit.Before;
import org.junit.Test;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.Result;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import org.molr.mole.core.testing.strand.AbstractSingleMissionStrandExecutorTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class ConcurrentStrandExecutorPauseTest extends AbstractSingleMissionStrandExecutorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentStrandExecutorPauseTest.class);

    private Block TASK_1;
    private Block TASK_2;
    private Block TASK_3;

    private CountDownLatch task1Start;
    private CountDownLatch task1Finish;
    private CountDownLatch task2Start;
    private CountDownLatch task2Finish;

    @Override
    protected RunnableLeafsMission mission() {
        return new RunnableLeafsMissionSupport() {
            {
                sequential("Pausing test", root -> {
                    TASK_1 = root.run("Long task1", () -> {
                        unlatch(task1Start);
                        await(task1Finish);
                    });
                    TASK_2 = root.run("Long task2", () -> {
                        unlatch(task2Start);
                        await(task2Finish);
                    });
                    TASK_3 = root.run("NOOP", () -> {
                    });
                });
            }
        }.build();
    }

    @Before
    public void setUp() {
        task1Start = new CountDownLatch(1);
        task1Finish = new CountDownLatch(1);
        task2Start = new CountDownLatch(1);
        task2Finish = new CountDownLatch(1);
    }

    @Test
    public void testPause() {
        instructRootStrandAsync(StrandCommand.RESUME);

        await(task1Start);
        instructRootStrandAsync(StrandCommand.PAUSE);
        unlatch(task1Finish);

        waitUntilRootStrandStateIs(RunState.PAUSED);
        assertThatResultOf(TASK_1).as("Task 1 should have finished before pausing").isEqualTo(Result.SUCCESS);
        assertThatResultOf(TASK_2).as("Task 2 should have not been run").isEqualTo(Result.UNDEFINED);
        assertThatRootStrandBlock().as("Executor should point to task 2").isEqualTo(TASK_2);

        instructRootStrandAsync(StrandCommand.RESUME);

        await(task2Start);
        instructRootStrandAsync(StrandCommand.PAUSE);
        unlatch(task2Finish);

        waitUntilRootStrandStateIs(RunState.PAUSED);
        assertThatResultOf(TASK_2).as("Task 2 should have finished before pausing").isEqualTo(Result.SUCCESS);
        assertThatResultOf(TASK_3).as("Task 3 should have not been run").isEqualTo(Result.UNDEFINED);
        assertThatRootStrandBlock().as("Executor should point to task 3").isEqualTo(TASK_3);

        instructRootStrandAsync(StrandCommand.RESUME);
        waitUntilRootStrandStateIs(RunState.FINISHED);

        assertThatResultOf(TASK_1).isEqualTo(Result.SUCCESS);
        assertThatResultOf(TASK_2).isEqualTo(Result.SUCCESS);
        assertThatResultOf(TASK_3).isEqualTo(Result.SUCCESS);
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }
}
