import org.junit.Before;
import org.junit.Test;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.Result;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import org.molr.mole.core.runnable.lang.Branch;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.molr.mole.core.tree.LeafExecutor;
import org.molr.mole.core.tree.TreeMissionExecutor;
import org.molr.mole.core.tree.TreeResultTracker;
import org.molr.mole.core.tree.TreeStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.molr.commons.domain.Result.SUCCESS;
import static org.molr.commons.domain.Result.UNDEFINED;

public class TreeSteppingTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeSteppingTest.class);

    private static Block FIRST;
    private static Block SECOND;
    private static Block THIRD;

    private final static RunnableLeafsMission DATA = new RunnableMissionSupport() {
        {
            mission("Root", root -> {

               FIRST = root.sequential("First", b -> {
                    b.run(log("First A"));
                    b.run(log("First B"));
                });

                SECOND = root.sequential("Second", b -> {
                    b.run(log("second A"));
                    b.run(log("second B"));
                });

                THIRD = root.run(log("Third"));

                root.parallel("Parallel", b -> {
                    b.run(log("Parallel A"));
                    b.run(log("parallel B"));
                });

            });

        }
    }.build();

    private TreeStructure treeStructure;
    private TreeMissionExecutor missionExecutor;
    private TreeResultTracker resultTracker;

    @Before
    public void setUp() {
        treeStructure = DATA.treeStructure();
        resultTracker = new TreeResultTracker(treeStructure.missionRepresentation());
        LeafExecutor leafExecutor = new RunnableBlockExecutor(resultTracker, DATA.runnables());
        missionExecutor = new TreeMissionExecutor(treeStructure, leafExecutor, resultTracker);
    }

    @Test
    public void stepInGetsBackOut() throws InterruptedException {
        Thread.sleep(500);
        missionExecutor.instruct(missionExecutor.getRootStrand(), StrandCommand.STEP_INTO);
        Thread.sleep(500);
        missionExecutor.instruct(missionExecutor.getRootStrand(), StrandCommand.STEP_OVER);
        Thread.sleep(500);
        missionExecutor.instruct(missionExecutor.getRootStrand(), StrandCommand.STEP_OVER);
        Thread.sleep(500);
        missionExecutor.instruct(missionExecutor.getRootStrand(), StrandCommand.STEP_OVER);
        Thread.sleep(500);

        logResultsOf(resultTracker, treeStructure);

        Thread.sleep(500);


        assertThat(resultFor(FIRST)).isEqualTo(SUCCESS);
        assertThat(resultFor(SECOND)).isEqualTo(SUCCESS);
        assertThat(resultFor(THIRD)).isEqualTo(UNDEFINED);
    }

    private Result resultFor(Block node) {
        return resultTracker.resultFor(node);
    }

    private static Branch.Task log(String text) {
        return new Branch.Task(text, () -> LOGGER.info("{} executed", text));
    }

    private static void logResultsOf(TreeResultTracker resultTracker, TreeStructure structure) {
        LOGGER.info("Results:");
        BiConsumer<Block, Integer> c = (b, depth) -> {
            String span = Arrays.stream(new int[depth]).mapToObj(a -> "\t").collect(Collectors.joining());
            LOGGER.info("{}{} -> {}", span, b.text(), resultTracker.resultFor(b));
        };
        visitParentBeforeChild(structure, c);
    }

    private static void visitParentBeforeChild(TreeStructure structure, BiConsumer<Block, Integer> c) {
        visitParentBeforeChild(structure.rootBlock(), 0, c, structure);
    }

    private static void visitParentBeforeChild(Block block, int depth, BiConsumer<Block, Integer> c, TreeStructure structure) {
        c.accept(block, depth);
        depth++;

        if (structure.isLeaf(block))
            return;

        for (Block child : structure.childrenOf(block)) {
            visitParentBeforeChild(child, depth, c, structure);
        }
    }

}
