import com.google.common.collect.ImmutableMap;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.runnable.ExecutionData;
import org.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.molr.mole.core.tree.LeafExecutor;
import org.molr.mole.core.tree.TreeMissionExecutor;
import org.molr.mole.core.tree.TreeResultTracker;
import org.molr.mole.core.tree.TreeStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SequenceTry {

    private static final Logger LOGGER = LoggerFactory.getLogger(SequenceTry.class);

    public static void main(String[] args) throws IOException {

        ExecutionData data = new RunnableMissionSupport() {
            {
                mission("Root");

                sequential("First", b -> {
                    b.println("First A");
                    b.println("First B");
                });

                sequential("Second", b -> {
                    b.println("second A");
                    b.println("second B");
                });

                println("Third");

                parallel("Parallel", b -> {
                    b.println("Parallel A");
                    b.println("parallel B");
                });

            }
        }.build();


        TreeStructure treeStructure = data.treeStructure();
        TreeResultTracker resultTracker = new TreeResultTracker(treeStructure.missionRepresentation());
        LeafExecutor leafExecutor = new RunnableBlockExecutor(resultTracker, createInstructions(treeStructure));
        TreeMissionExecutor mission = new TreeMissionExecutor(treeStructure, leafExecutor, resultTracker);

//        mission.instruct(mission.getStrand(), StrandCommand.STEP_INTO);
//        mission.instruct(mission.getStrand(), StrandCommand.STEP_OVER);
        //mission.instruct(mission.getStrand(), StrandCommand.STEP_OVER);
//        mission.instruct(null, StrandCommand.RESUME);
        mission.instruct(null, StrandCommand.STEP_OVER);


        logResultsOf(resultTracker, treeStructure);
    }

    private static Map<Block, Runnable> createInstructions(TreeStructure treeStructure) {
        ImmutableMap.Builder<Block, Runnable> builder = ImmutableMap.builder();

        visitParentBeforeChild(treeStructure, block -> builder.put(block, () -> LOGGER.info("{} executed", block.text())));

        return builder.build();
    }

    private static void logResultsOf(TreeResultTracker resultTracker, TreeStructure structure) {
        LOGGER.info("Results:");
        BiConsumer<Block, Integer> c = (b, depth) -> {
            String span = Arrays.stream(new int[depth]).mapToObj(a -> "\t").collect(Collectors.joining());
            LOGGER.info("{}{} -> {}", span, b.text(), resultTracker.resultFor(b));
        };
        visitParentBeforeChild(structure, c);
    }

    private static void visitParentBeforeChild(TreeStructure structure, Consumer<Block> c) {
        visitParentBeforeChild(structure, (block, depth) -> c.accept(block));
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
