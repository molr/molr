package io.molr.mole.core.runnable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.molr.commons.domain.*;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.tree.TreeStructure;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static io.molr.mole.core.runnable.lang.BranchMode.PARALLEL;

public class RunnableLeafsMission {

    private final ImmutableMap<Block, BiConsumer<In, Out>> runnables;
    private final TreeStructure treeStructure;
    private final MissionParameterDescription parameterDescription;

    private RunnableLeafsMission(Builder builder, MissionParameterDescription parameterDescription) {
        this.runnables = builder.runnables.build();
        MissionRepresentation representation = builder.representationBuilder.build();
        this.treeStructure = new TreeStructure(representation, builder.parallelBlocksBuilder.build());
        this.parameterDescription = parameterDescription;
    }

    public TreeStructure treeStructure() {
        return this.treeStructure;
    }

    public MissionParameterDescription parameterDescription() {
        return this.parameterDescription;
    }

    public Map<Block, BiConsumer<In, Out>> runnables() {
        return this.runnables;
    }

    public String name() {
        return this.treeStructure.rootBlock().text();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final AtomicLong nextId = new AtomicLong(0);
        private final AtomicReference<Block> latest = new AtomicReference<>();

        private ImmutableMissionRepresentation.Builder representationBuilder;
        private final ImmutableMap.Builder<Block, BiConsumer<In, Out>> runnables = ImmutableMap.builder();
        private final ImmutableSet.Builder<Block> parallelBlocksBuilder = ImmutableSet.builder();

        private Builder() {

        }

        public Block createRoot(String rootName, BranchMode branchMode) {
            if (representationBuilder != null) {
                throw new IllegalStateException("root cannot be defined twice!");
            }

            Block root = block(rootName);
            if (PARALLEL == branchMode) {
                parallelBlocksBuilder.add(root);
            }
            this.representationBuilder = ImmutableMissionRepresentation.builder(root);
            this.latest.set(root);
            return root;
        }

        public Block sequentialChild(Block parent, String childName) {
            return addChild(parent, childName);
        }

        public Block parallelChild(Block parent, String childName) {
            Block child = addChild(parent, childName);
            parallelBlocksBuilder.add(child);
            return child;
        }

        public Block leafChild(Block parent, String childName, BiConsumer<In, Out> runnable) {
            Block child = addChild(parent, childName);
            runnables.put(child, runnable);
            return child;
        }

        public Block root() {
            assertRootDefined();
            return representationBuilder.root();
        }

        public RunnableLeafsMission build(MissionParameterDescription parameterDescription) {
            return new RunnableLeafsMission(this, parameterDescription);
        }

        private Block addChild(Block parent, String childName) {
            assertRootDefined();

            Block child = block(childName);
            representationBuilder.parentToChild(parent, child);
            latest.set(child);
            return child;
        }

        private void assertRootDefined() {
            if (this.representationBuilder == null) {
                throw new IllegalStateException("No root node defined yet!");
            }
        }

        /**
         * Retrieves the latest created block. This is intended mainly for testing.
         *
         * @return the most recently created (added) block.
         */
        public Block latest() {
            return latest.get();
        }

        private Block block(String name) {
            return Block.idAndText("" + nextId.getAndIncrement(), name);
        }

        public void breakOn(Block block) {
            representationBuilder.addDefaultBreakpoint(block);
        }
    }
}
