package io.molr.mole.core.tree.executor;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.BlockAttribute;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import io.molr.mole.core.testing.strand.AbstractSingleMissionStrandExecutorTest;

/**
 * @author krepp
 */
public class OnErrorForceQuitTest extends AbstractSingleMissionStrandExecutorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnErrorForceQuitTest.class);

    @Override
    public Logger logger() {
        return LOGGER;
    }

    @Override
    public void setUpAbstract() {
        /* ConcurrentStrandExecutor needs to be initialized with test case specific ExecutionStrategy */
    }

    @Override
    protected RunnableLeafsMission mission() {
        return new RunnableLeafsMissionSupport() {
            {

                root("TestMission").as(rootBranch -> {// 0
                    rootBranch.leaf("task1").run(() -> {// 0.0
                        LOGGER.info("run task 1");
                    });
                    rootBranch.leaf("task2").perDefault(BlockAttribute.ON_ERROR_SKIP_SEQUENTIAL_SIBLINGS).run(() -> {
                        // Can we replace sibling attribute?
                        LOGGER.info("run task 2");
                        throw new RuntimeException("Task2 failed");
                    });
                    rootBranch.leaf("task3").run(() -> {
                        LOGGER.info("run task 3");
                    });
                });
            }
        }.build();
    }

    @Test
    public void singleStrandProceedOnErrorTest() {
        setUpAbstract(ExecutionStrategy.PROCEED_ON_ERROR);
        instructRootStrandSync(StrandCommand.RESUME);

        waitUntilRootStrandStateIs(RunState.FINISHED);

        assertThatResultOf(Block.builder("0", "TestMission").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.0", "task1").build()).isEqualTo(Result.SUCCESS);
        assertThatResultOf(Block.builder("0.1", "task2").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.2", "task3").build()).isEqualTo(Result.UNDEFINED);
    }

    @Test
    public void onErrorForceQuit_whenSetOnSequentialBranchWithError_succeedingBranchIsNotExecuted() {

        RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
            {

                root("TestMission").as(rootBranch -> {// 0
                    rootBranch.branch("0.0").perDefault(BlockAttribute.FORCE_ABORT_ON_ERROR).as(zeroDotZero -> {
                        zeroDotZero.leaf("task1").run(() -> {/* nothing */});
                        zeroDotZero.leaf("task2").perDefault(BlockAttribute.ON_ERROR_SKIP_SEQUENTIAL_SIBLINGS)
                                .run(() -> {
                                    throw new RuntimeException("Task2 failed");
                                });
                        zeroDotZero.leaf("task3").run(() -> {/* nothing */});
                    });
                    rootBranch.leaf("0.1").run(() -> {/* nothing */});

                });
            }
        }.build();

        setUpAbstract(ExecutionStrategy.PROCEED_ON_ERROR, mission);

        instructRootStrandSync(StrandCommand.RESUME);

        waitUntilRootStrandStateIs(RunState.FINISHED);

        assertThatResultOf(Block.builder("0", "TestMission").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.0", "0.0").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.0.0", "task1").build()).isEqualTo(Result.SUCCESS);
        assertThatResultOf(Block.builder("0.0.1", "task2").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.1", "0.1").build()).isEqualTo(Result.UNDEFINED);
    }

}
