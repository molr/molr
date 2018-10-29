import org.junit.Test;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import org.molr.mole.core.runnable.lang.RunnableBranchSupport;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.molr.mole.core.tree.LeafExecutor;
import org.molr.mole.core.tree.StrandExecutor;
import org.molr.mole.core.tree.StrandFactory;
import org.molr.mole.core.tree.StrandFactoryImpl;
import org.molr.mole.core.tree.TreeResultTracker;
import org.molr.mole.core.tree.TreeStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
                    PARALLEL_A = b.run(log("Parallel A"));
                    PARALLEL_B = b.run(log("parallel B"));
                });

            });

        }
    }.build();

    @Test
    public void testMovement() throws InterruptedException, ExecutionException, TimeoutException {
        TreeStructure treeStructure = DATA.treeStructure();
        TreeResultTracker resultTracker = new TreeResultTracker(treeStructure.missionRepresentation());
        LeafExecutor leafExecutor = new RunnableBlockExecutor(resultTracker, DATA.runnables());

        StrandFactory strandFactory = new StrandFactoryImpl();
        Strand rootStrand = strandFactory.rootStrand();
        Block rootBlock = treeStructure.rootBlock();

        StrandExecutor strandExecutor = new StrandExecutor(rootStrand, rootBlock, treeStructure, strandFactory, leafExecutor);

        strandExecutor.getStateStream().subscribe(s -> LOGGER.info("Current state: {}", s));
        strandExecutor.getBlockStream().subscribe(b -> LOGGER.info("Current block: {}", b));

        strandExecutor.instruct(StrandCommand.STEP_INTO);
        strandExecutor.instruct(StrandCommand.SKIP);
        strandExecutor.instruct(StrandCommand.SKIP);
        strandExecutor.instruct(StrandCommand.SKIP);
        strandExecutor.instruct(StrandCommand.STEP_OVER);
//        LOGGER.info("PARALLEL RESULT {}", parallelFuture.get(5, TimeUnit.SECONDS));
//        strandExecutor.instruct(StrandCommand.STEP_INTO);
//        strandExecutor.instruct(StrandCommand.STEP_OVER);

        sleep(1000);
        System.out.println(strandExecutor);
    }

    @Test
    public void testSubstructure() {
        TreeStructure treeStructure = DATA.treeStructure();

        TreeStructure substructure = treeStructure.substructure(FIRST_A);
        LOGGER.info("FIRST_A {}", substructure.allBlocks());

        substructure = treeStructure.substructure(FIRST);
        LOGGER.info("FIRST {}", substructure.allBlocks());
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private static RunnableBranchSupport.Task log(String text) {
        return new RunnableBranchSupport.Task(text, () -> LOGGER.info("{} executed", text));
    }
}
