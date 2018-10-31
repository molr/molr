package org.molr.mole.core.tree;

import org.junit.Test;
import org.molr.commons.domain.Block;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.lang.RunnableBranchSupport;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class TreeStructureTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeStructureTest.class);

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
                    PARALLEL_A = b.run(log("parallel A"));
                    PARALLEL_B = b.run(log("parallel B"));
                });

                FOURTH = root.sequential("Fourth", b -> {
                    FOURTH_A = b.run(log("Fourth"));
                });
            });

        }
    }.build();

    @Test
    public void testSubtreeOfLeafContainsJustTheLeaf() {
        TreeStructure structure = DATA.treeStructure();
        structure.allBlocks().stream().filter(structure::isLeaf).forEach(leaf -> {
            TreeStructure substructure = structure.substructure(leaf);
            assertThat(substructure.allBlocks()).hasSize(1);
            assertThat(substructure.rootBlock()).isEqualTo(leaf);
            assertThat(substructure.allBlocks().iterator().next()).isEqualTo(leaf);
        });
    }

    @Test
    public void testSubtrees() {
        TreeStructure structure = DATA.treeStructure();
        TreeStructure firstSubtree = structure.substructure(FIRST);
        TreeStructure secondSubtree = structure.substructure(SECOND);
        TreeStructure parallelSubtree = structure.substructure(PARALLEL);

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

        assertThat(structure.substructure(structure.rootBlock())).isEqualTo(structure);
    }

    @Test
    public void testNextOfLastIsNull() {
        TreeStructure tree = DATA.treeStructure();
        assertThat(tree.nextBlock(FOURTH)).isEmpty();
        assertThat(tree.nextBlock(FOURTH_A)).isEmpty();
    }

    @Test
    public void testNextOfSequenceIsNextSibling() {
        TreeStructure tree = DATA.treeStructure();
        assertThat(tree.nextBlock(FIRST)).contains(SECOND);
        assertThat(tree.nextBlock(SECOND)).contains(THIRD);
        assertThat(tree.nextBlock(THIRD)).contains(PARALLEL);
        assertThat(tree.nextBlock(PARALLEL)).contains(FOURTH);

        assertThat(tree.nextBlock(PARALLEL_A)).contains(PARALLEL_B);
        assertThat(tree.nextBlock(FIRST_A)).contains(FIRST_B);
        assertThat(tree.nextBlock(FIRST_A)).contains(FIRST_B);
        assertThat(tree.nextBlock(SECOND_A)).contains(SECOND_B);
    }

    @Test
    public void testNextOfLastChildIsNextOfParent() {
        TreeStructure tree = DATA.treeStructure();
        assertThat(tree.nextBlock(FIRST_B)).contains(SECOND);
        assertThat(tree.nextBlock(SECOND_B)).contains(THIRD);
        assertThat(tree.nextBlock(PARALLEL_B)).contains(FOURTH);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubstructureOfUnknownBlockThrows() {
        DATA.treeStructure().substructure(Block.idAndText("unknown", "unknown"));
    }

    private static RunnableBranchSupport.Task log(String text) {
        return new RunnableBranchSupport.Task(text, () -> LOGGER.info("{} executed", text));
    }

}
