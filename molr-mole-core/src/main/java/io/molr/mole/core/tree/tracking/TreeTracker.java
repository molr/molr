package io.molr.mole.core.tree.tracking;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MissionRepresentation;
import reactor.core.publisher.Flux;

public class TreeTracker<T> implements Bucket<T>, Tracker<T> {

    private final MissionRepresentation representation;

    private final Map<Block, BlockTracker<T>> blockResultTrackers;

    private final T defaultValue;
    private final Function<Iterable<T>, T> summarizer;
    private final Flux<? extends Entry<Block, T>> mergedUpdateStream;

    private TreeTracker(Builder<T> builder) {
        this.representation = builder.representation;
        this.defaultValue = builder.defaultValue;
        this.summarizer = builder.summarizer;
        this.blockResultTrackers = builder.createBlockTrackers();

        List<Flux<? extends Entry<Block, T>>> allUpdateStreams = blockResultTrackers.entrySet().stream()
        		.map(entry -> entry.getValue().asStream()
        				.map(update->new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), update))).collect(Collectors.toList());
        mergedUpdateStream = Flux.merge(allUpdateStreams);
    }

    public static <T> TreeTracker<T> create(MissionRepresentation representation, T defaultValue, Function<Iterable<T>, T> summarizer) {
        return new Builder<>(representation, defaultValue, summarizer, null).build();
    }

    public static <T> TreeTracker<T> create(MissionRepresentation representation, TreeTracker<T> oldTracker) {
        return new Builder<>(representation, oldTracker.defaultValue, oldTracker.summarizer, oldTracker).build();
    }


    @Override
    public void push(Block node, T result) {
        if (!representation.isLeaf(node)) {
            throw new IllegalArgumentException("publishing results is only allowed for leaves.");
        }
        BlockTracker<T> blockTracker = blockResultTrackers.get(node);
        if (blockTracker == null) {
            throw new IllegalStateException("No block tracker found for block '" + node + "'.");
        }
        if (blockTracker instanceof LeafTracker) {
            ((LeafTracker<T>) blockTracker).push(result);
        } else {
            throw new IllegalStateException("Block tracker for block '" + node + "' is not a leaf tracker.");
        }
    }

    @Override
    public T resultFor(Block block) {
        return Optional.ofNullable(blockResultTrackers.get(block)).map(r -> r.result()).orElse(null);
    }

    public Flux<Block> updatedBlocksStream() {
        return mergedUpdateStream.map(entry->entry.getKey());
    }
    
    public Flux<T> resultUpdatesFor(Block block) {
        return blockResultTrackers.get(block).asStream();
    }

    @Override
    public Map<Block, T> blockResults() {
        return this.blockResultTrackers.entrySet().stream().collect(toMap(e -> e.getKey(), e -> e.getValue().result()));
    }

    public static class Builder<T> {

        private final MissionRepresentation representation;
        private final TreeTracker<T> oldTracker;
        private final T defaultValue;
        private final Function<Iterable<T>, T> summarizer;

        private Builder(MissionRepresentation representation, T defaultValue, Function<Iterable<T>, T> summarizer, TreeTracker<T> oldTracker) {
            this.representation = Objects.requireNonNull(representation, "representation must not be null");
            this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue must not be null");
            this.summarizer = Objects.requireNonNull(summarizer, "summarizer must not be null");
            this.oldTracker = oldTracker;
        }


        private TreeTracker<T> build() {
            return new TreeTracker<>(this);
        }

        private Map<Block, BlockTracker<T>> createBlockTrackers() {
            Block block = representation.rootBlock();
            HashMap<Block, BlockTracker<T>> map = new HashMap<>();
            addTrackerForBlock(block, map);
            return Collections.unmodifiableMap(map);
        }

        private void addTrackerForBlock(Block block, Map<Block, BlockTracker<T>> map) {
            if (representation.isLeaf(block)) {
                map.put(block, new LeafTracker<>(initialValueFor(block)));
            } else {
                representation.childrenOf(block).forEach(b -> addTrackerForBlock(b, map));
                List<Flux<T>> childrenResults = representation.childrenOf(block).stream()
                        .map(map::get)
                        .map(BlockTracker::asStream).collect(toList());
                map.put(block, BlockCombiner.combine(childrenResults, initialValueFor(block), summarizer));
            }
        }

        private T initialValueFor(Block block) {
            if (this.oldTracker != null) {
                T initialValue = oldTracker.resultFor(block);
                if (initialValue != null) {
                    return initialValue;
                }
            }
            return defaultValue;
        }

    }

}
