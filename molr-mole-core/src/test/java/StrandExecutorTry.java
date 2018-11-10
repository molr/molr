import org.junit.Test;
import org.molr.commons.domain.*;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import org.molr.mole.core.runnable.lang.Branch;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.molr.mole.core.tree.*;
import org.molr.mole.core.tree.tracking.TreeTracker;
import org.molr.mole.core.utils.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class StrandExecutorTry {

    private static final Logger LOGGER = LoggerFactory.getLogger(StrandExecutorTry.class);

    private static Block FIRST;
    private static Block FIRST_A;
    private static Block FIRST_B;
    private static Block SECOND;
    private static Block SECOND_A;
    private static Block SECOND_B;
    private static Block THIRD;
    private static Block PARALLEL;
    private static Block PARALLEL_A;
    private static Block PARALLEL_B;

    private final static RunnableLeafsMission DATA = new RunnableMissionSupport() {
        {
            mission("Root", root -> {

                FIRST = root.sequential("First", b -> {
                    FIRST_A = b.run(log("First A"));
                    FIRST_B = b.run(log("First B"));
                });

                SECOND = root.sequential("Second", b -> {
                    SECOND_A = b.run(log("second A"));
                    SECOND_B = b.run(log("second B"));
                });

                THIRD = root.run(log("Third"));

                PARALLEL = root.parallel("Parallel", b -> {
                    PARALLEL_A = b.sequential("parallel A", b1 -> {
                       b1.run(log("parallel A.1"));
                       b1.run(log("parallel A.2"));
                       b1.run(log("parallel A.3"));
                       b1.run(log("parallel A.4"));
                       b1.run(log("parallel A.5"));
                    });
                    PARALLEL_B = b.run(log("parallel B"));
                });

            });

        }
    }.build();

    @Test
    public void testMovement() throws InterruptedException {
        TreeStructure treeStructure = DATA.treeStructure();
        TreeTracker resultTracker = TreeTracker.create(treeStructure.missionRepresentation(), Result.UNDEFINED, Result::summaryOf);
        TreeTracker<RunState> runStateTracker = TreeTracker.create(treeStructure.missionRepresentation(), RunState.UNDEFINED, RunState::summaryOf);

        LeafExecutor leafExecutor = new RunnableBlockExecutor(resultTracker, DATA.runnables(),  MissionInput.empty(), new ConcurrentMissionOutputCollector(), runStateTracker);

        StrandFactory strandFactory = new StrandFactoryImpl();
        StrandExecutorFactory strandExecutorFactory = new StrandExecutorFactory(strandFactory, leafExecutor);
        Strand rootStrand = strandFactory.rootStrand();
        Block rootBlock = treeStructure.rootBlock();

        StrandExecutor strandExecutor = new ConcurrentStrandExecutor(rootStrand, rootBlock, treeStructure, strandFactory, strandExecutorFactory, leafExecutor);

        CountDownLatch finishedLatch = new CountDownLatch(1);

        strandExecutor.getStateStream().subscribe(s -> {
//            LOGGER.info("Current state: {}", s);
            if(s == RunState.FINISHED) {
                finishedLatch.countDown();
            }
        });
//        rootStrandExecutor.getBlockStream().subscribe(b -> LOGGER.info("Current block: {}", b));

//        rootStrandExecutor.instruct(StrandCommand.RESUME);
//        sleep(2000);
//        rootStrandExecutor.instruct(StrandCommand.PAUSE);
//        sleep(2000);
//        rootStrandExecutor.instruct(StrandCommand.RESUME);

        // Execute just the parallel
//        rootStrandExecutor.instruct(StrandCommand.STEP_INTO);
//        rootStrandExecutor.instruct(StrandCommand.SKIP);
//        rootStrandExecutor.instruct(StrandCommand.SKIP);
//        rootStrandExecutor.instruct(StrandCommand.SKIP);
//        rootStrandExecutor.instruct(StrandCommand.STEP_OVER);

//        rootStrandExecutor.instruct(StrandCommand.STEP_INTO);
//        sleep(1000);
//        rootStrandExecutor.instruct(StrandCommand.RESUME);
//        sleep(8000);
//        rootStrandExecutor.instruct(StrandCommand.PAUSE);
//        sleep(1000);
//        rootStrandExecutor.instruct(StrandCommand.RESUME);

        strandExecutor.instruct(StrandCommand.STEP_OVER);
        sleep(8000);
        strandExecutor.instruct(StrandCommand.PAUSE);
        sleep(1000);
        strandExecutorFactory._getStrandExecutorByStrandId("1").get().instruct(StrandCommand.RESUME);

        sleep(8000);

        strandExecutor.instruct(StrandCommand.STEP_OVER);

        assertThat(finishedLatch.await(60, TimeUnit.SECONDS)).isTrue();


        RunState runState = strandExecutor.getStateStream().blockFirst();
        System.out.println(runState);

        System.out.println(strandExecutor);


        TreeUtils.logResultsOf(resultTracker, treeStructure);
    }

    @Test
    public void testSubstructure() {
        TreeStructure treeStructure = DATA.treeStructure();

        for (Block child : treeStructure.childrenOf(PARALLEL)) {
            LOGGER.info("{}", treeStructure.substructure(child).allBlocks());
        }

    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private static Branch.Task log(String text) {
        return new Branch.Task(text, () -> LOGGER.info("{} executed", text));
    }
}
