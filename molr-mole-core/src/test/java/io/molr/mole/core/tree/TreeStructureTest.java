package io.molr.mole.core.tree;

import io.molr.commons.domain.Block;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import io.molr.mole.core.utils.Trees;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TreeStructureTest {

    private static final Runnable NOOP = () -> {
    };

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
    private static Block FOURTH;
    private static Block FOURTH_A;

    private final static RunnableLeafsMission DATA = new RunnableLeafsMissionSupport() {
        {
            root("Root").sequential().as(root -> {

                root.branch("First").sequential().as(b1 -> {
                    FIRST = latestBlock();

                    b1.leaf("First A").run(NOOP);
                    FIRST_A = latestBlock();

                    b1.leaf("First B").run(NOOP);
                    FIRST_B = latestBlock();
                });

                root.branch("Second").sequential().as(b1 -> {
                    SECOND = latestBlock();

                    b1.leaf("second A").run(NOOP);
                    SECOND_A = latestBlock();

                    b1.leaf("second B").run(NOOP);
                    SECOND_B = latestBlock();
                });

                root.leaf("Third").run(NOOP);
                THIRD = latestBlock();

                root.branch("Parallel").parallel().as(b1 -> {
                    PARALLEL = latestBlock();

                    b1.leaf("parallel A").run(NOOP);
                    PARALLEL_A = latestBlock();

                    b1.leaf("parallel B").run(NOOP);
                    PARALLEL_B = latestBlock();
                });

                root.branch("Fourth").sequential().as(b -> {
                    FOURTH = latestBlock();

                    b.leaf("Fourth").run(NOOP);
                    FOURTH_A = latestBlock();
                });
            });

        }
    }.build();

    private static final TreeStructure STRUCTURE = DATA.treeStructure();

    @Test
    public void testSubtreeOfLeafContainsJustTheLeaf() {
        STRUCTURE.allBlocks().stream().filter(STRUCTURE::isLeaf).forEach(leaf -> {
            TreeStructure substructure = STRUCTURE.substructure(leaf);
            assertThat(substructure.allBlocks()).hasSize(1);
            assertThat(substructure.rootBlock()).isEqualTo(leaf);
            assertThat(substructure.allBlocks().iterator().next()).isEqualTo(leaf);
        });
    }

    @Test
    public void testSubtrees() {
        TreeStructure firstSubtree = STRUCTURE.substructure(FIRST);
        TreeStructure secondSubtree = STRUCTURE.substructure(SECOND);
        TreeStructure parallelSubtree = STRUCTURE.substructure(PARALLEL);

        assertThat(firstSubtree.rootBlock()).isEqualTo(FIRST);
        assertThat(firstSubtree.allBlocks()).containsExactlyInAnyOrder(FIRST, FIRST_A, FIRST_B);
        assertThat(firstSubtree.isLeaf(FIRST_A)).isTrue();
        assertThat(firstSubtree.isLeaf(FIRST_B)).isTrue();

        assertThat(secondSubtree.rootBlock()).isEqualTo(SECOND);
        assertThat(secondSubtree.allBlocks()).containsExactlyInAnyOrder(SECOND, SECOND_A, SECOND_B);
        assertThat(secondSubtree.isLeaf(SECOND_A)).isTrue();
        assertThat(secondSubtree.isLeaf(SECOND_B)).isTrue();

        assertThat(parallelSubtree.rootBlock()).isEqualTo(PARALLEL);
        assertThat(parallelSubtree.allBlocks()).containsExactlyInAnyOrder(PARALLEL, PARALLEL_A, PARALLEL_B);
        assertThat(parallelSubtree.isLeaf(PARALLEL_A)).isTrue();
        assertThat(parallelSubtree.isLeaf(PARALLEL_B)).isTrue();

        assertThat(STRUCTURE.substructure(STRUCTURE.rootBlock())).isEqualTo(STRUCTURE);
    }

    @Test
    public void testNextOfLastIsNull() {
        assertThat(STRUCTURE.nextBlock(FOURTH)).isEmpty();
        assertThat(STRUCTURE.nextBlock(FOURTH_A)).isEmpty();
    }

    @Test
    public void testNextOfSequenceIsNextSibling() {
        assertThat(STRUCTURE.nextBlock(FIRST)).contains(SECOND);
        assertThat(STRUCTURE.nextBlock(SECOND)).contains(THIRD);
        assertThat(STRUCTURE.nextBlock(THIRD)).contains(PARALLEL);
        assertThat(STRUCTURE.nextBlock(PARALLEL)).contains(FOURTH);

        assertThat(STRUCTURE.nextBlock(PARALLEL_A)).contains(PARALLEL_B);
        assertThat(STRUCTURE.nextBlock(FIRST_A)).contains(FIRST_B);
        assertThat(STRUCTURE.nextBlock(FIRST_A)).contains(FIRST_B);
        assertThat(STRUCTURE.nextBlock(SECOND_A)).contains(SECOND_B);
    }

    @Test
    public void testNextOfLastChildIsNextOfParent() {
        assertThat(STRUCTURE.nextBlock(FIRST_B)).contains(SECOND);
        assertThat(STRUCTURE.nextBlock(SECOND_B)).contains(THIRD);
        assertThat(STRUCTURE.nextBlock(PARALLEL_B)).contains(FOURTH);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubstructureOfUnknownBlockThrows() {
        STRUCTURE.substructure(Block.idAndText("unknown", "unknown"));
    }

    /* Testing tree utils.. */
    @Test
    public void testBlockHasParallelParent() {
        List<Block> withoutParallelParentNodes = Arrays.asList(FIRST, FIRST_A, FIRST_B, SECOND, SECOND_A, SECOND_B, THIRD, PARALLEL, FOURTH, FOURTH_A);
        List<Block> withParallelParentNodes = Arrays.asList(PARALLEL_A, PARALLEL_B);

        withoutParallelParentNodes.forEach(node -> assertThat(Trees.doesBlockHaveAParallelParent(node, STRUCTURE))
                .as("Block %s should not have a parallel parent", node).isFalse());
        withParallelParentNodes.forEach(node -> assertThat(Trees.doesBlockHaveAParallelParent(node, STRUCTURE))
                .as("Block %s should have a parallel parent", node).isTrue());
    }

}
