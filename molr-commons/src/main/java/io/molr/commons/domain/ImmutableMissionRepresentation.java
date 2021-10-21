package io.molr.commons.domain;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;

public final class ImmutableMissionRepresentation implements MissionRepresentation {

    private final Block root;
    private final ListMultimap<Block, Block> children;
    private final ListMultimap<Block, BlockAttribute> blockAttributes;

    public ImmutableMissionRepresentation(Builder builder) {
        this.root = builder.rootBlock;
        this.children = builder.treeBuilder.build();
        this.blockAttributes = builder.blockAttributes.build();
    }

    @Override
    public Block rootBlock() {
        return this.root;
    }

    @Override
    public List<Block> childrenOf(Block block) {
        return children.get(block);
    }

    @Override
    public Set<Block> allBlocks() {
        HashSet<Block> blocks = new HashSet<>();
        blocks.add(root);
        for (Block parent : children.keys()) {
            blocks.add(parent);
            blocks.addAll(children.get(parent));
        }
        return blocks;
    }

    @Override
    public boolean isLeaf(Block block) {
        return childrenOf(block).isEmpty();
    }

    @Override
    public Optional<Block> parentOf(Block block) {
        if (block.equals(root)) {
            return Optional.empty();
        }

        return children.entries().stream().filter(e -> e.getValue().equals(block)).map(Map.Entry::getKey).findFirst();
    }

    @Override
    public ListMultimap<Block, Block> parentsToChildren() {
        return this.children;
    }

	@Override
	public ListMultimap<Block, BlockAttribute> blockAttributes() {
		return this.blockAttributes;
	}

    public static Builder builder(Block rootBlock) {
        return new Builder(rootBlock);
    }

    public static Builder builder(MissionRepresentation oldRepresentation) {
        return builder(oldRepresentation.rootBlock()).parentsToChildren(oldRepresentation.parentsToChildren())
        		.addBlockAttributes(oldRepresentation.blockAttributes());
    }

    public static MissionRepresentation empty(String name) {
        return builder(Block.idAndText("0", name)).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableMissionRepresentation that = (ImmutableMissionRepresentation) o;
        return Objects.equals(root, that.root) &&
                Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root, children);
    }

    public static class Builder {

        private final Block rootBlock;
        private final ImmutableListMultimap.Builder<Block, Block> treeBuilder = ImmutableListMultimap.builder();
        ImmutableSet.Builder<Block> defaultBreakpointsBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<Block> defaultIgnoreBlocksBuilder = ImmutableSet.builder();
        ImmutableListMultimap.Builder<Block, BlockAttribute> blockAttributes = ImmutableListMultimap.builder();
        
        public Builder addBlockAttributes(ListMultimap<Block, BlockAttribute> attributes) {
        	requireNonNull(attributes);
        	blockAttributes.putAll(attributes);
        	return this;
        }
        
        public Builder addBlockAttributes(Block block, Collection<BlockAttribute> attributes) {
        	blockAttributes.putAll(block, attributes);
        	return this;
        }
        
        public Builder addDefaultBreakpoint(final Block block) {
            defaultBreakpointsBuilder.add(block);
            return this;
        }

        public Builder addDefaultBreakpoints(final Collection<Block> blocks) {
            defaultBreakpointsBuilder.addAll(blocks);
            return this;
        }
        
        private Builder(Block rootBlock) {
            this.rootBlock = requireNonNull(rootBlock, "rootBlock must not be null");
        }
        
        public Builder addDefaultIgnoreBlock(final Block block) {
            defaultIgnoreBlocksBuilder.add(block);
            return this;
        }

        public Builder addDefaultIgnoreBlocks(final Collection<Block> blocks) {
            defaultIgnoreBlocksBuilder.addAll(blocks);
            return this;
        }

        public Builder parentToChild(Block parent, Block child) {
            treeBuilder.put(parent, child);
            return this;
        }

        public Builder parentsToChildren(ListMultimap<Block, Block> tree) {
            this.treeBuilder.putAll(tree);
            return this;
        }

        public Block root() {
            return this.rootBlock;
        }

        public MissionRepresentation build() {
            return new ImmutableMissionRepresentation(this);
        }
    }

}
