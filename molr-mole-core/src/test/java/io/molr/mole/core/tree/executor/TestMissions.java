package io.molr.mole.core.tree.executor;

import java.util.function.BiConsumer;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.ImmutableMissionRepresentation;
import io.molr.commons.domain.In;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.tree.TreeStructure;

public class TestMissions {

    final static Block block_0 = Block.idAndText("0", "ROOT");
    final Block block_00 = Block.idAndText("0.0", "BLOCK_0.0");
    final Block block_01 = Block.idAndText("0.1", "BLOCK_0.1");
    final Block block_02 = Block.idAndText("0.2", "BLOCK_0.2");

    public static void main(String args[]) {

        TestTreeContext testConfig = TestTreeContext.builder(testRepresentation(2, 3)).parallel("0")
                .ignore("0.0", "0.1").build();
        testConfig.strandExecutor().instruct(StrandCommand.RESUME);

        System.out.println(TreeStructure.print(testConfig.treeStructure()));
        RunState lastState = testConfig.strandExecutor().getStateStream().blockLast();
        System.out.println(lastState);
    }

    public static MissionRepresentation testRepresentation(int depth, int numOfChildrenPerBranch) {
        Block root = Block.idAndText("0", "0");
        ImmutableMissionRepresentation.Builder builder = ImmutableMissionRepresentation.builder(root);
        populate(root, depth, numOfChildrenPerBranch, builder);
        return builder.build();
    }

    private static void populate(Block root, int depthLeft, int numOfChildren,
            ImmutableMissionRepresentation.Builder builder) {
        if (depthLeft == 0) {
            return;
        }
        for (int i = 0; i < numOfChildren; i++) {
            Block child = Block.idAndText(root.id() + "." + i, root.id() + "." + i);
            builder.parentToChild(root, child);
            populate(child, depthLeft - 1, numOfChildren, builder);
        }
    }

    public static BiConsumer<In, Out> defaultRunnable(Block block) {
        return (in, out) -> {
            System.out.println("execute " + block);
        };
    }

    public static BiConsumer<In, Out> throwingRunnable(Block block) {
        return (in, out) -> {
            System.out.println("execute " + block + " throw exception");
        };
    }

}
