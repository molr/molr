package io.molr.mole.core.tree;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import io.molr.mole.core.testing.strand.AbstractSingleMissionStrandExecutorTest;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author krepp
 */
public class ConcurrentStrandExecutorExecutionStrategyTest extends AbstractSingleMissionStrandExecutorTest{

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentStrandExecutorExecutionStrategyTest.class);
    
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
        return new RunnableLeafsMissionSupport() {
            {
                
                root("TestMission").as(rootBranch -> {
                    rootBranch.leaf("task1").run(()->{
                        LOGGER.info("run task 1");
                    });
                    rootBranch.leaf("task2").run(()->{
                        LOGGER.info("run task 2");
                        throw new RuntimeException("Task2 failed");
                    });
                    rootBranch.leaf("task3").run(()->{
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
        System.out.println("instructed");
        
        waitUntilRootStrandStateIs(RunState.FINISHED);
        System.out.println("finished");
        
        assertThatResultOf(Block.builder("0", "TestMission").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.0", "task1").build()).isEqualTo(Result.SUCCESS);
        assertThatResultOf(Block.builder("0.1", "task2").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.2", "task3").build()).isEqualTo(Result.SUCCESS);
        System.out.println("ready");
    }
    
    @Test
    public void singleStrandAbortOnErrorTest() {
        setUpAbstract(ExecutionStrategy.ABORT_ON_ERROR);
        instructRootStrandSync(StrandCommand.RESUME);
        
        waitUntilRootStrandStateIs(RunState.FINISHED);
        
        assertThatResultOf(Block.builder("0", "TestMission").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.0", "task1").build()).isEqualTo(Result.SUCCESS);
        assertThatResultOf(Block.builder("0.1", "task2").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.2", "task3").build()).isEqualTo(Result.UNDEFINED);                      
    }
    
    @Test(timeout = 500)
    public void singleStrandPauseOnErrorTest() {
        setUpAbstract(ExecutionStrategy.PAUSE_ON_ERROR);
        instructRootStrandSync(StrandCommand.RESUME);
        
        waitUntilRootStrandStateIs(RunState.PAUSED);

        assertThatResultOf(Block.builder("0.0", "task1").build()).isEqualTo(Result.SUCCESS);
        assertThatResultOf(Block.builder("0.1", "task2").build()).isEqualTo(Result.FAILED);
        assertThatResultOf(Block.builder("0.2", "task3").build()).isEqualTo(Result.UNDEFINED);
        assertThatResultOf(Block.builder("0", "TestMission").build()).isEqualTo(Result.FAILED);                   
    }
    
}

