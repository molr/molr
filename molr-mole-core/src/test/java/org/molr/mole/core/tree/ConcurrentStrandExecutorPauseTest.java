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

import static org.assertj.core.api.Assertions.assertThat;

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
        return new RunnableMissionSupport() {
            {
                mission("Pausing test", root -> {
                    TASK_1 = root.run("Long task1", () -> {
                        task1Start.countDown();
                        await(task1Finish);
                    });
                    TASK_2 = root.run("Long task2", () -> {
                        task2Start.countDown();
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
        strandExecutor().instruct(StrandCommand.RESUME);

        await(task1Start);
        strandExecutor().instruct(StrandCommand.PAUSE);
        task1Finish.countDown();

        waitForStateToBe(strandExecutor(), RunState.PAUSED);
        assertThat(treeResultTracker().resultFor(TASK_1)).isEqualTo(Result.SUCCESS)
                .as("Task 1 should have finished before pausing");
        assertThat(treeResultTracker().resultFor(TASK_2)).isEqualTo(Result.UNDEFINED)
                .as("Task 2 should have not been run");
        assertThat(strandExecutor().getActualBlock()).isEqualTo(TASK_2)
                .as("Executor should point to task 2");

        strandExecutor().instruct(StrandCommand.RESUME);

        await(task2Start);
        strandExecutor().instruct(StrandCommand.PAUSE);
        task2Finish.countDown();

        waitForStateToBe(strandExecutor(), RunState.PAUSED);
        assertThat(treeResultTracker().resultFor(TASK_2)).isEqualTo(Result.SUCCESS)
                .as("Task 2 should have finished before pausing");
        assertThat(treeResultTracker().resultFor(TASK_3)).isEqualTo(Result.UNDEFINED)
                .as("Task 3 should have not been run");
        assertThat(strandExecutor().getActualBlock()).isEqualTo(TASK_3)
                .as("Executor should point to task 3");

        strandExecutor().instruct(StrandCommand.RESUME);
        waitForStateToBe(strandExecutor(), RunState.FINISHED);

        assertThat(treeResultTracker().resultFor(TASK_1)).isEqualTo(Result.SUCCESS);
        assertThat(treeResultTracker().resultFor(TASK_2)).isEqualTo(Result.SUCCESS);
        assertThat(treeResultTracker().resultFor(TASK_3)).isEqualTo(Result.SUCCESS);
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }
}
