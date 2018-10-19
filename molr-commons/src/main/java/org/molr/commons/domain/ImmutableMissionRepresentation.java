package org.molr.commons.domain;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ImmutableMissionRepresentation implements MissionRepresentation {

    private final Block root;
    private final ListMultimap<Block, Block> children;

    public ImmutableMissionRepresentation(Builder builder) {
        this.root = builder.rootBlock;
        this.children = builder.treeBuilder.build();
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
        for (Block block : children.keys()) {
            blocks.add(block);
            blocks.addAll(children.get(block));
        }
        return blocks;
    }

    @Override
    public boolean isLeaf(Block block) {
        return childrenOf(block).isEmpty();
    }

    public static Builder builder(Block rootBlock) {
        return new Builder(rootBlock);
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

        private Builder(Block rootBlock) {
            this.rootBlock = requireNonNull(rootBlock, "rootBlock must not be null");
        }

        public Builder parentToChild(Block parent, Block child) {
            treeBuilder.put(parent, child);
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
