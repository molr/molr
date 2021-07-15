package io.molr.mole.core.tree.executor;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.BlockAttribute;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.OngoingSimpleBranch;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import io.molr.mole.core.testing.strand.AbstractSingleMissionStrandExecutorTest;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author krepp
 */
public class ExecutionStrategyInParallelChildrenTest extends AbstractSingleMissionStrandExecutorTest{

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionStrategyInParallelChildrenTest.class);
    
    @Override
    public Logger logger() {
        return LOGGER;
    }

    @Override
    public void setUpAbstract() {
        /*ConcurrentStrandExecutor needs to be initialized with test case specific ExecutionStrategy*/
    }
    
    @Override
    protected RunnableLeafsMission mission() {
        return mission(false);
    }

    private RunnableLeafsMission mission(boolean forceParallelBranchToQuit) {
        return new RunnableLeafsMissionSupport() {
            {
                executionStrategy().allowAll().defaultsTo(ExecutionStrategy.ABORT_ON_ERROR);
                root("TestMission").as(rootBranch -> {//0
                	OngoingSimpleBranch parallelBranch = rootBranch.branch("parallelBranch").parallel();
                	if(forceParallelBranchToQuit) {
                		parallelBranch = parallelBranch.perDefault(BlockAttribute.FORCE_ABORT_ON_ERROR);
                	}
                	
                	parallelBranch.as(parallel->{//0.0
                        parallel.leaf("task1").run(()->{//0.0.0
                            LOGGER.info("run task 1");
                        });
								parallel.leaf("task2")/* .perDefault(BlockAttribute.ON_ERROR_FORCE_QUIT) */.run(()->{
                            LOGGER.info("run task 2");
                            throw new RuntimeException("Task2 failed");
                        });
                        parallel.leaf("task3").run(()->{
                            LOGGER.info("run task 3");
                        });
                	});
                	rootBranch.leaf("task4").run(()->{
                		LOGGER.info("run task 4");
                	});
                });
            }
        }.build();
    }
    
    @Test
    public void abortOnError_abortOnErrorAtParallelBranch_missionIsAbortedAfterParallelBranchExecution() {
        setUpAbstract(ExecutionStrategy.ABORT_ON_ERROR);
        instructRootStrandSync(StrandCommand.RESUME);
        System.out.println("instructed");
        
        waitUntilRootStrandStateIs(RunState.FINISHED);
        System.out.println("finished");
        
        assertThatResultOf(Block.builder("0", "TestMission").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.0.0", "task1").build()).isEqualTo(Result.SUCCESS);
        assertThatResultOf(Block.builder("0.0.1", "task2").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.0.2", "task3").build()).isEqualTo(Result.SUCCESS);
        assertThatResultOf(Block.builder("0.1", "task4").build()).isEqualTo(Result.UNDEFINED);
    }
    
    @Test
    public void singleStrandProceedOnError_parallelBranchIsNotForcedToQuit_missionRunsThrough() {
        setUpAbstract(ExecutionStrategy.PROCEED_ON_ERROR, mission(false));
        instructRootStrandSync(StrandCommand.RESUME);
        
        waitUntilRootStrandStateIs(RunState.FINISHED);
        
        assertThatResultOf(Block.builder("0", "TestMission").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.0.0", "task1").build()).isEqualTo(Result.SUCCESS);
        assertThatResultOf(Block.builder("0.0.1", "task2").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.0.2", "task3").build()).isEqualTo(Result.SUCCESS);
        assertThatResultOf(Block.builder("0.1", "task4").build()).isEqualTo(Result.SUCCESS);
    }
    
    @Test
    public void singleStrandProceedOnErrorButForceQuit_parallelBranchIsForcedToQuit_missionIsAbortedAfterParallelBranchExecution() {
        setUpAbstract(ExecutionStrategy.PROCEED_ON_ERROR, mission(true));
        instructRootStrandSync(StrandCommand.RESUME);
        
        waitUntilRootStrandStateIs(RunState.FINISHED);
        
        assertThatResultOf(Block.builder("0", "TestMission").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.0.0", "task1").build()).isEqualTo(Result.SUCCESS);
        assertThatResultOf(Block.builder("0.0.1", "task2").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.0.2", "task3").build()).isEqualTo(Result.SUCCESS);
        assertThatResultOf(Block.builder("0.1", "task4").build()).isEqualTo(Result.UNDEFINED);
    }
    
}

