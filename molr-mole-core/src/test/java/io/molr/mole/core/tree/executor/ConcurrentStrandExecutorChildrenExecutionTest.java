package io.molr.mole.core.tree.executor;

import static io.molr.commons.domain.RunState.FINISHED;
import static io.molr.commons.domain.RunState.PAUSED;
import static io.molr.commons.domain.RunState.RUNNING;
import static io.molr.commons.domain.StrandCommand.PAUSE;
import static io.molr.commons.domain.StrandCommand.RESUME;
import static io.molr.commons.domain.StrandCommand.STEP_INTO;
import static io.molr.commons.domain.StrandCommand.STEP_OVER;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import io.molr.mole.core.testing.strand.AbstractSingleMissionStrandExecutorTest;
import io.molr.mole.core.testing.strand.StrandExecutorTestSupport;
import io.molr.mole.core.tree.StrandExecutor;

public class ConcurrentStrandExecutorChildrenExecutionTest extends AbstractSingleMissionStrandExecutorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentStrandExecutorStepOverParallelTest.class);

    private Block parallelBlock;
    private Block lastBlock;
    private Block blockA2;
    private Block blockB2;

    private CountDownLatch latchA1Start;
    private CountDownLatch latchB1Start;
    private CountDownLatch latchB2Start;
    private CountDownLatch latchA1End;
    private CountDownLatch latchB1End;
    private CountDownLatch latchB2End;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);
    
    @Override
    protected RunnableLeafsMission mission() {
        return new RunnableLeafsMissionSupport() {
            {
                root("step-over").sequential().as(root -> {
                    root.branch("parallel").parallel().as(b -> {
                        parallelBlock = latestBlock();

                        b.branch("sequential branch A").sequential().as(bA -> {
                            bA.leaf("A.1").run(() -> {
                                unlatch(latchA1Start);
                                await(latchA1End);
                            });
                            bA.leaf("A.2").run(() -> {
                            });
                            blockA2 = latestBlock();
                        });
                        b.branch("sequential branch B").sequential().as(bB -> {
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
    }

    @Test
    public void testStepOverLastChildrenAfterStepIntoPausesAtParentSibling() throws InterruptedException {
        moveRootStrandTo(parallelBlock);
        System.out.println("MOVED to "+rootStrandExecutor().getActualBlock());
        instructRootStrandSync(STEP_INTO);
        System.out.println("STEP_INTO called");
        rootStrandChildren().forEach(se -> waitUntilStrandStateIs(se, PAUSED));
        LOGGER.info("Children paused");
        System.out.println("rootStrandChildren"+rootStrandChildren());
        //me
        rootStrandChildren().forEach(se -> StrandExecutorTestSupport.instructSync(se, STEP_OVER));
        await(latchA1Start);
        await(latchB1Start);
        
        unlatch(latchA1End, latchB1End, latchB2End);
        System.out.println("rootStrandChildren"+rootStrandChildren());

        waitUntilRootStrandBlockIs(lastBlock);
        waitUntilRootStrandStateIs(PAUSED);

        System.out.println("waited");
        
        assertThatRootStrandBlock().isEqualTo(lastBlock);
        assertThatRootStrandState().isNotEqualTo(FINISHED);
    }
    
    
    
//
    @Test
    public void testChildrenFinishWhileParentIsPauseShouldFinishParent() {
        moveRootStrandTo(parallelBlock);
        instructRootStrandSync(StrandCommand.RESUME);

        await(latchA1Start, latchB1Start);
        instructRootStrandSync(PAUSE);
        
        System.out.println("instructed \n\n\n\n");
        unlatch(latchA1End, latchB1End, latchB2End);
        System.out.println("instructed \n\n\n\n");
        waitUntilRootStrandStateIs(PAUSED);
        System.out.println("paused \n\n\n\n");
        rootStrandChildren().forEach(se -> waitUntilStrandStateIs(se, PAUSED));

        rootStrandChildren().forEach(se -> se.instruct(StrandCommand.RESUME));
        rootStrandChildren().forEach(this::waitUntilStrandIsFinished);

        waitUntilRootStrandBlockIs(lastBlock);
        assertThatRootStrandBlock().isEqualTo(lastBlock);
        assertThatRootStrandState().isNotEqualTo(FINISHED);
    }

    @Test
    public void testChildrenFinishWhileParentIsPauseShouldMoveNextOnParent() {
        moveRootStrandTo(parallelBlock);
        instructRootStrandSync(StrandCommand.STEP_OVER);

        await(latchA1Start, latchB1Start);
        instructRootStrandSync(PAUSE);
        unlatch(latchA1End, latchB1End, latchB2End);

        waitUntilRootStrandStateIs(PAUSED);
        rootStrandChildren().forEach(se -> waitUntilStrandStateIs(se, PAUSED));

        rootStrandChildren().forEach(se -> se.instruct(StrandCommand.RESUME));
        rootStrandChildren().forEach(this::waitUntilStrandIsFinished);

        waitUntilRootStrandBlockIs(lastBlock);
    }

    /**
     * is this the right way to do, should children really control parent?
     */
    @Test
    public void testIfAllChildrenArePausedParentShouldPause() {
        moveRootStrandTo(parallelBlock);
        instructRootStrandSync(StrandCommand.RESUME);

        await(latchA1Start, latchB1Start);
        rootStrandChildren().forEach(se -> instructAsync(se, PAUSE));
        unlatch(latchA1End, latchB1End, latchB2End);

        rootStrandChildren().forEach(se -> waitUntilStrandStateIs(se, PAUSED));
        waitUntilRootStrandStateIs(PAUSED);

        rootStrandChildren().forEach(se -> assertThatStateOf(se).isEqualTo(PAUSED));
    }

    @Test
    public void testIfAtLeastOneChildrenIsRunningParentIsWaiting() {
        /* WAITING_FOR_CHILDREN == RUNNING RunState */
        moveRootStrandTo(parallelBlock);
        instructRootStrandSync(StrandCommand.RESUME);

        await(latchA1Start, latchB1Start);
        instructRootStrandSync(PAUSE);
        unlatch(latchA1End, latchB1End);

        waitUntilRootStrandStateIs(PAUSED);

        /* at this point the children must be at these blocks*/
        StrandExecutor strandA = rootStrandChildren().stream()
                .filter(se -> se.getActualBlock().equals(blockA2)).findFirst().get();

        StrandExecutor strandB = rootStrandChildren().stream()
                .filter(se -> se.getActualBlock().equals(blockB2)).findFirst().get();

        waitUntilStrandStateIs(strandA, PAUSED);
        waitUntilStrandStateIs(strandB, PAUSED);
        assertThatStateOf(strandA).isEqualTo(PAUSED);
        assertThatStateOf(strandB).isEqualTo(PAUSED);

        instructAsync(strandB, RESUME);
        await(latchB2Start);

        System.out.println("wait?");
        
        waitUntilRootStrandStateIs(RUNNING);

        assertThatStateOf(strandA).isEqualTo(PAUSED);
        assertThatStateOf(strandB).isEqualTo(RUNNING);
        assertThatRootStrandState().isEqualTo(RUNNING);
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }
}
