package io.molr.mole.core.utils;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.molr.commons.domain.Block;
import io.molr.mole.core.tree.TreeStructure;
import io.molr.mole.core.tree.tracking.TreeTracker;

public class Trees {

    private static final Logger LOGGER = LoggerFactory.getLogger(Trees.class);

    /**
     * Walks through the {@link TreeStructure}, writing the result of each node to the logger.
     * 
     * @param tracker the tracker whose results to write to the logger
     * @param structure the structure to walk through
     */
    public static void logResultsOf(TreeTracker<?> tracker, TreeStructure structure) {
        LOGGER.info("Results:");
        BiConsumer<Block, Integer> c = (b, depth) -> {
            String span = Arrays.stream(new int[depth]).mapToObj(a -> "\t").collect(Collectors.joining());
            LOGGER.info("{}{} -> {}", span, b.text(), tracker.resultFor(b));
        };
        visitParentBeforeChild(structure, c);
    }

    /**
     * Walk through the provided {@link TreeStructure} visiting the parent before each child. The {@link BiConsumer} is
     * applied to each node and the second parameter represents the current depth of the node in the tree.
     * 
     * @param structure the structure to walk through
     * @param c a biconsumer which is called for each block, with the block as first parameter and the depth in the tree
     *            as second.
     */
    public static void visitParentBeforeChild(TreeStructure structure, BiConsumer<Block, Integer> c) {
        visitParentBeforeChild(structure.rootBlock(), 0, c, structure);
    }

    private static void visitParentBeforeChild(Block block, int depth, BiConsumer<Block, Integer> c,
            TreeStructure structure) {
        c.accept(block, depth);
        int nextDepth = depth + 1;

        if (structure.isLeaf(block))
            return;

        for (Block child : structure.childrenOf(block)) {
            visitParentBeforeChild(child, nextDepth, c, structure);
        }
    }

    /**
     * Return whether or not the block has a parent in the hierarchy of the provided {@link TreeStructure} that is
     * parallel
     * 
     * @param block the block for which to query if it has a parent which is parallel
     * @param structure the tree structure to consider
     * @return {@code true} if the block has a parent which is parallel, {@code false} if not.
     */
    public static boolean doesBlockHaveAParallelParent(Block block, TreeStructure structure) {
        Optional<Block> parent = structure.parentOf(block);
        if (parent.isPresent()) {
            if (structure.isParallel(parent.get())) {
                return true;
            }
            return doesBlockHaveAParallelParent(parent.get(), structure);
        }
        return false;
    }

}
