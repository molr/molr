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

public class StrandExecutorTry {

    private static final Logger LOGGER = LoggerFactory.getLogger(StrandExecutorTry.class);

    private final static RunnableLeafsMission DATA = new RunnableMissionSupport() {
        {
            mission("Root", root -> {

                root.sequential("First", b -> {
                    b.run(log("First A"));
                    b.run(log("First B"));
                });

                root.sequential("Second", b -> {
                    b.run(log("second A"));
                    b.run(log("second B"));
                });

                root.run(log("Third"));

                root.parallel("Parallel", b -> {
                    b.run(log("Parallel A"));
                    b.run(log("parallel B"));
                });

            });

        }
    }.build();

    @Test
    public void testMovement() {
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
        strandExecutor.instruct(StrandCommand.STEP_OVER);

        sleep(1000);
        System.out.println(strandExecutor);
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
