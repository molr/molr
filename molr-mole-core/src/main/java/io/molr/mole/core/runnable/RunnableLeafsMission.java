package io.molr.mole.core.runnable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.molr.commons.domain.*;
import io.molr.mole.core.runnable.lang.BlockAttribute;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.tree.TreeStructure;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static io.molr.mole.core.runnable.lang.BranchMode.PARALLEL;
import static java.util.Objects.requireNonNull;

public class RunnableLeafsMission {

    private final ImmutableMap<Block, BiConsumer<In, Out>> runnables;
    private final ImmutableMap<Block, BiConsumer<In, Out>> forEachRunnables;
    private final ImmutableMap<Block, ForEachConfiguration<?,?>> forEachConfigurations;
    private final ImmutableMap<Block, ForEachConfiguration<?,?>> forEachBlocksConfigurations;
    private final ImmutableMap<Block, Placeholder<?>> forEachBlocks;
    private final TreeStructure treeStructure;
    private final MissionParameterDescription parameterDescription;
    private final Function<In, ?> contextFactory;
    
    private RunnableLeafsMission(Builder builder, MissionParameterDescription parameterDescription) {
        this.runnables = builder.runnables.build();
        this.forEachRunnables = builder.forEachRunnables.build();
        this.forEachConfigurations = builder.forEachConfigurations.build();
        this.forEachBlocksConfigurations = builder.forEachBlocksConfigurations.build();
        this.forEachBlocks = builder.forEachBLocks.build();
        MissionRepresentation representation = builder.representationBuilder.build();
        this.treeStructure = new TreeStructure(representation, builder.parallelBlocksBuilder.build());
        this.parameterDescription = parameterDescription;
        this.contextFactory = builder.contextFactory;
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
    

    public Map<Block, BiConsumer<In, Out>> forEachRunnables() {
        return this.forEachRunnables;
    }
    
    public Map<Block, ForEachConfiguration<?,?>> forEachConfigurations() {
        return this.forEachConfigurations;
    }

    public Map<Block, Placeholder<?>> getForEachBlocks() {
		return forEachBlocks;
	}

	public String name() {
        return this.treeStructure.rootBlock().text();
    }

    public Function<In, ?> contextFactory() {
        return this.contextFactory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ImmutableMap<Block, ForEachConfiguration<?,?>> getForEachBlocksConfigurations() {
		return forEachBlocksConfigurations;
	}

	public static class Builder {

        private final AtomicLong nextId = new AtomicLong(0);
        private final AtomicReference<Block> latest = new AtomicReference<>();

        private ImmutableMissionRepresentation.Builder representationBuilder;
        private final ImmutableMap.Builder<Block, BiConsumer<In, Out>> runnables = ImmutableMap.builder();
        private final ImmutableMap.Builder<Block, BiConsumer<In, Out>> forEachRunnables = ImmutableMap.builder();
        private final ImmutableMap.Builder<Block, ForEachConfiguration<?,?>> forEachConfigurations = ImmutableMap.builder();
        private final ImmutableMap.Builder<Block, Placeholder<?>> forEachBLocks = ImmutableMap.builder();
        private final ImmutableMap.Builder<Block, ForEachConfiguration<?,?>> forEachBlocksConfigurations = ImmutableMap.builder();
        private final ImmutableSet.Builder<Block> parallelBlocksBuilder = ImmutableSet.builder();

        private Function<In, ?> contextFactory;

        private Builder() {
            /* use static factory method */
        }

        public Block rootBranchNode(String rootName, BranchMode branchMode, Set<BlockAttribute> blockAttributes) {
            if (representationBuilder != null) {
                throw new IllegalStateException("root cannot be defined twice!");
            }

            Block root = block(rootName);
            if (PARALLEL == branchMode) {
                parallelBlocksBuilder.add(root);
            }
            this.representationBuilder = ImmutableMissionRepresentation.builder(root);
            apply(root, blockAttributes);
            this.latest.set(root);
            return root;
        }

        public Block childBranchNode(Block parent, String name, BranchMode mode, Set<BlockAttribute> blockAttributes) {
            Block child = addChild(parent, name, blockAttributes);
            if (mode == PARALLEL) {
                parallelBlocksBuilder.add(child);
            }
            return child;
        }

        public Block leafChild(Block parent, String childName, BiConsumer<In, Out> runnable, Set<BlockAttribute> blockAttributes) {
            Block child = addChild(parent, childName, blockAttributes);
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

        private Block addChild(Block parent, String childName, Set<BlockAttribute> blockAttributes) {
            assertRootDefined();

            Block child = block(childName);
            representationBuilder.parentToChild(parent, child);
            apply(child, blockAttributes);
            latest.set(child);
            return child;
        }

        private void apply(Block block, Set<BlockAttribute> blockAttributes) {
            if (blockAttributes.contains(BlockAttribute.BREAK)) {
                representationBuilder.addDefaultBreakpoint(block);
            }
        }

        private void assertRootDefined() {
            if (this.representationBuilder == null) {
                throw new IllegalStateException("No root node defined yet!");
            }
        }

        public void contextFactory(Function<In, ?> contextFactory) {
            if (this.contextFactory != null) {
                throw new IllegalStateException("contextFactory already set! Only allowed once!");
            }
            this.contextFactory = requireNonNull(contextFactory, "contextFactory must not be null");
        }

        /**
         * Retrieves the latest created block. This is intended mainly for testing.
         *
         * @return the most recently created (added) block.
         */
        public Block latest() {
            return latest.get();
        }

        //TODO remove public access
        public Block block(String name) {
            return Block.idAndText("" + nextId.getAndIncrement(), name);
        }

        public <T,U> void forEach(String name, Block parent, Placeholder<T> devicesPlaceholder, Placeholder<U> itemPlaceholder, BiConsumer<In, Out> itemConsumer) {
              Block block = addChild(parent, name, ImmutableSet.of());
              ForEachConfiguration<T, U> config = new ForEachConfiguration<>(devicesPlaceholder, itemPlaceholder, itemConsumer);
              forEachConfigurations.put(block,config);
              forEachRunnables.put(block, itemConsumer);
        }

		public <T, U> void forEachBlock(Block block, Placeholder<T> collectionPlaceholder, Placeholder<U> itemPlaceholder) {
			forEachBLocks.put(block, collectionPlaceholder);
            ForEachConfiguration<T, U> forEachBlockConfiguration = new ForEachConfiguration<>(collectionPlaceholder, itemPlaceholder, null);
            forEachBlocksConfigurations.put(block, forEachBlockConfiguration);
            
		}
    }
}
