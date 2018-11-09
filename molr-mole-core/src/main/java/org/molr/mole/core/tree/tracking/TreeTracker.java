package org.molr.mole.core.tree.tracking;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.MissionRepresentation;
import org.molr.commons.domain.Result;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.toList;
import static org.molr.commons.domain.Result.UNDEFINED;

public class TreeTracker implements Bucket<Result>, Tracker<Result> {

    private final MissionRepresentation representation;

    private final Map<Block, BlockTracker<Result>> blockResultTrackers;

    private final Result defaultValue;
    private final Function<Iterable<Result>, Result> summarizer;

    public TreeTracker(MissionRepresentation representation, Result defaultValue, Function<Iterable<Result>, Result> summarizer) {
        this.representation = representation;
        this.defaultValue = defaultValue;
        this.summarizer = summarizer;
        this.blockResultTrackers = createBlockTrackers();
    }

    private Map<Block, BlockTracker<Result>> createBlockTrackers() {
        Block block = representation.rootBlock();
        HashMap<Block, BlockTracker<Result>> map = new HashMap<>();
        addTrackerForBlock(block, map);
        return Collections.unmodifiableMap(map);
    }

    private void addTrackerForBlock(Block block, Map<Block, BlockTracker<Result>> map) {
        if (representation.isLeaf(block)) {
            map.put(block, new LeafTracker(UNDEFINED));
        } else {
            representation.childrenOf(block).forEach(b -> addTrackerForBlock(b, map));
            List<Flux<Result>> childrenResults = representation.childrenOf(block).stream()
                    .map(map::get)
                    .map(BlockTracker::asStream).collect(toList());
            map.put(block, BlockCombiner.combine(childrenResults, UNDEFINED, Result::summaryOf));
        }
    }

    @Override
    public void push(Block node, Result result) {
        if (!representation.isLeaf(node)) {
            throw new IllegalArgumentException("publishing results is only allowed for leaves.");
        }
        BlockTracker blockTracker = blockResultTrackers.get(node);
        if (blockTracker == null) {
            throw new IllegalStateException("No block tracker found for block '" + node + "'.");
        }
        if (blockTracker instanceof LeafTracker) {
            ((LeafTracker) blockTracker).push(result);
        } else {
            throw new IllegalStateException("Block tracker for block '" + node + "' is not a leaf tracker.");
        }
    }

    @Override
    public Result resultFor(Block block) {
        return blockResultTrackers.get(block).result();
    }

    public Flux<Result> resultUpdatesFor(Block block) {
        return blockResultTrackers.get(block).asStream();
    }

    @Override
    public Map<Block, Result> blockResults() {
        return this.blockResultTrackers.entrySet().stream().collect(toImmutableMap(e -> e.getKey(), e -> e.getValue().result()));
    }

}
