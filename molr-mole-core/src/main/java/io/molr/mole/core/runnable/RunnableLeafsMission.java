package io.molr.mole.core.runnable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.molr.commons.domain.*;
import io.molr.mole.core.tree.TreeStructure;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

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

    public static Builder sequentialRoot(String rootName) {
        return new Builder(rootName, false);
    }

    public static Builder parallelRoot(String rootName) {
        return new Builder(rootName, true);
    }

    public static class Builder {

        private final AtomicLong nextId = new AtomicLong(0);

        private final ImmutableMissionRepresentation.Builder representationBuilder;
        private final ImmutableMap.Builder<Block, BiConsumer<In, Out>> runnables = ImmutableMap.builder();
        private final ImmutableSet.Builder<Block> parallelBlocksBuilder = ImmutableSet.builder();

        private Builder(String rootName, boolean parallel) {
            Block root = block(rootName);
            if (parallel) {
                parallelBlocksBuilder.add(root);
            }
            representationBuilder = ImmutableMissionRepresentation.builder(root);
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
            return representationBuilder.root();
        }

        public RunnableLeafsMission build(MissionParameterDescription parameterDescription) {
            return new RunnableLeafsMission(this, parameterDescription);
        }

        private Block addChild(Block parent, String childName) {
            Block child = block(childName);
            representationBuilder.parentToChild(parent, child);
            return child;
        }

        private Block block(String name) {
            return Block.idAndText("" + nextId.getAndIncrement(), name);
        }
    }
}
