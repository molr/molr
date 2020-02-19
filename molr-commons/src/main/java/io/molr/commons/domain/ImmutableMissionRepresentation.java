package io.molr.commons.domain;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

public final class ImmutableMissionRepresentation implements MissionRepresentation {

    private final Block root;
    private final ListMultimap<Block, Block> children;
    private Set<Block> breakpoints;

    public ImmutableMissionRepresentation(Builder builder) {
        this.root = builder.rootBlock;
        this.children = builder.treeBuilder.build();
        this.breakpoints = builder.breakpoints;
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

    public static Builder builder(Block rootBlock) {
        return new Builder(rootBlock);
    }

    public static Builder builder(MissionRepresentation oldRepresentation) {
        return builder(oldRepresentation.rootBlock()).parentsToChildren(oldRepresentation.parentsToChildren());
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
        private final Set<Block> breakpoints = Collections.newSetFromMap(new ConcurrentHashMap<Block, Boolean>());
        
        public void addBreakpoint(Block block) {
            this.breakpoints.add(block);
        }
        
        private Builder(Block rootBlock) {
            this.rootBlock = requireNonNull(rootBlock, "rootBlock must not be null");
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

    @Override
    public Set<Block> breakpoints() {
        return breakpoints;
    }


}
