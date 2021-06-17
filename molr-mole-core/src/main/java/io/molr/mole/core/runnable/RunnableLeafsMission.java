package io.molr.mole.core.runnable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import io.molr.commons.domain.*;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.BlockNameConfiguration;
import io.molr.mole.core.tree.TreeStructure;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

import java.util.Collection;
import java.util.List;

public class RunnableLeafsMission {

	private final static String ROOT_BLOCK_ID = "0";
	
    private final ImmutableMap<Block, BiConsumer<In, Out>> runnables;
    private final ImmutableMap<Block, ForEachConfiguration<?,?>> forEachBlocksConfigurations;
    private final ImmutableMap<Block, ContextConfiguration> contexts;
    private final ImmutableMap<Block,List<Placeholder<?>>> blockNameFormatterArgs;
    private final TreeStructure treeStructure;
    private final MissionParameterDescription parameterDescription;
    private final ImmutableMap<Block, Integer> maxConcurrency;
    private final ImmutableMap<Block, Map<Placeholder<?>, Function<In, ?>>> blockLetValues;
    
    
    private RunnableLeafsMission(Builder builder, MissionParameterDescription parameterDescription) {
        this.runnables = builder.runnables.build();
        this.forEachBlocksConfigurations = builder.forEachBlocksConfigurations.build();
        this.blockNameFormatterArgs = builder.blockNameFormatterArgumentBuilder.build();
        MissionRepresentation representation = builder.representationBuilder.build();
        this.maxConcurrency = builder.maxConurrencyConfiguration.build();
        this.treeStructure = new TreeStructure(representation, builder.parallelBlocksBuilder.build(), maxConcurrency);
        this.parameterDescription = parameterDescription;
        this.contexts = builder.contextConfigurations.build();
        blockLetValues = builder.blockLetValues.build();
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

    public List<Placeholder<?>> blockNameFormatterArgs(Block block){
    	return blockNameFormatterArgs.get(block);
    }
    
	public String name() {
        return this.treeStructure.rootBlock().text();
    }

//    public Function<In, ?> contextFactory() {
//        return this.contextFactory;
//    }
    
    public Map<Block, Integer> maxConcurrency(){
    	return this.maxConcurrency;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ImmutableMap<Block, ForEachConfiguration<?,?>> forEachBlocksConfigurations() {
		return forEachBlocksConfigurations;
	}
    
	public Map<Block, ContextConfiguration> contexts() {
		return this.contexts;
	}
	
	public Map<Block, Map<Placeholder<?>, Function<In, ?>>> letValues(){
		return blockLetValues; 
	}

	public static class Builder {

        private final AtomicReference<Block> latest = new AtomicReference<>();

        private final ListMultimap<Block, Block> parentToChildren = LinkedListMultimap.create();
        private ImmutableMissionRepresentation.Builder representationBuilder;
        private final ImmutableMap.Builder<Block, BiConsumer<In, Out>> runnables = ImmutableMap.builder();
        private final ImmutableMap.Builder<Block, ContextConfiguration> contextConfigurations = ImmutableMap.builder();
        private final ImmutableMap.Builder<Block, ForEachConfiguration<?,?>> forEachBlocksConfigurations = ImmutableMap.builder();
        private final ImmutableMap.Builder<Block, List<Placeholder<?>>> blockNameFormatterArgumentBuilder = ImmutableMap.builder();
        private final ImmutableSet.Builder<Block> parallelBlocksBuilder = ImmutableSet.builder();
        private final ImmutableMap.Builder<Block, Integer> maxConurrencyConfiguration = ImmutableMap.builder();
        private final ImmutableMap.Builder<Block, Map<Placeholder<?>, Function<In, ?>>> blockLetValues = ImmutableMap.builder();

        private Function<In, ?> contextFactory;

        private Builder() {
            /* use static factory method */
        }

        public Block rootBranchNode(BlockNameConfiguration rootName, BranchMode branchMode, Set<BlockAttribute> blockAttributes) {
            if (representationBuilder != null) {
                throw new IllegalStateException("root cannot be defined twice!");
            }

            Block root = block(ROOT_BLOCK_ID, rootName.text());
            if (BranchMode.Mode.PARALLEL == branchMode.mode()) {
                parallelBlocksBuilder.add(root);
                maxConurrencyConfiguration.put(root, branchMode.maxConcurrency());
            }
            this.representationBuilder = ImmutableMissionRepresentation.builder(root);
            apply(root, blockAttributes);
            this.latest.set(root);
            return root;
        }

        public Block childBranchNode(Block parent, BlockNameConfiguration name, BranchMode branchMode, Set<BlockAttribute> blockAttributes) {
            Block child = addChild(parent, name, blockAttributes);
            if (branchMode.mode()==BranchMode.Mode.PARALLEL) {
                parallelBlocksBuilder.add(child);
                maxConurrencyConfiguration.put(child, branchMode.maxConcurrency());
            }
            return child;
        }

        public Block leafChild(Block parent, BlockNameConfiguration childName, BiConsumer<In, Out> runnable, Set<BlockAttribute> blockAttributes) {
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

        private Block addChild(Block parent, BlockNameConfiguration childName, Set<BlockAttribute> blockAttributes) {
            assertRootDefined();

            int childId = parentToChildren.get(parent).size();
            Block child = block(parent.id()+"."+childId, childName.text());
            blockNameFormatterArgumentBuilder.put(child, childName.placeholders());
            parentToChildren.put(parent, child);
            representationBuilder.parentToChild(parent, child);
            apply(child, blockAttributes);
            latest.set(child);
            return child;
        }

        private void apply(Block block, Set<BlockAttribute> blockAttributes) {
        	representationBuilder.addBlockAttributes(block, blockAttributes);
        }

        private void assertRootDefined() {
            if (this.representationBuilder == null) {
                throw new IllegalStateException("No root node defined yet!");
            }
        }

        public void contextFactory(Block block, Placeholder<?> contextPlaceholder, Function<In, ?> contextFactory) {
            if (this.contextFactory != null) {
                throw new IllegalStateException("contextFactory already set! Only allowed once!");
            }
            this.contextConfigurations.put(block, new ContextConfiguration(contextFactory, contextPlaceholder));
        }

        /**
         * Retrieves the latest created block. This is intended mainly for testing.
         *
         * @return the most recently created (added) block.
         */
        public Block latest() {
            return latest.get();
        }

        private Block block(String id, String name) {
            return Block.idAndText(id, name);
        }
        
		public <T, U> void forEachBlock(Block block, Placeholder<? extends Collection<T>> collectionPlaceholder, Placeholder<T> itemPlaceholder) {
			forEachBlock(block, collectionPlaceholder, itemPlaceholder, itemPlaceholder, (in)->{return in.get(itemPlaceholder);});            
		}
        
		public <T, U> void forEachBlock(Block block, Placeholder<? extends Collection<T>> collectionPlaceholder, Placeholder<T> itemPlaceholder, Placeholder<U> transformedItemPlaceholder, Function<In, U> function) {
            ForEachConfiguration<T, U> forEachBlockConfiguration = new ForEachConfiguration<>(collectionPlaceholder, itemPlaceholder, transformedItemPlaceholder, function);
            forEachBlocksConfigurations.put(block, forEachBlockConfiguration);
            
		}

		public <T> void blockTextFormat(Block block, List<Placeholder<?>> placeholders) {
			blockNameFormatterArgumentBuilder.put(block, placeholders);
		}

		public void forEachConfig(Block block, ForEachConfiguration<?, ?> config) {
			forEachBlocksConfigurations.put(block, config);
			
		}

		public void addContextConfiguration(Block block, ContextConfiguration contextConfiguration) {
			contextConfigurations.put(block, contextConfiguration);
		}
		
		public void addBlockLetValues(Block block, Map<Placeholder<?>, Function<In, ?>> letValues) {
			this.blockLetValues.put(block, letValues);
		}
    }
}
