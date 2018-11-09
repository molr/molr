package org.molr.mole.core.tree;

import com.google.common.collect.ImmutableMap;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.MissionRepresentation;
import org.molr.commons.domain.Result;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.toList;

public class TreeResultTracker implements ResultBucket, ResultTracker {

    private final MissionRepresentation representation;

    private final Map<Block, BlockResultTracker> blockResultTrackers;

    public TreeResultTracker(MissionRepresentation representation) {
        this.representation = representation;
        this.blockResultTrackers = createBlockTrackers();
    }

    private Map<Block, BlockResultTracker> createBlockTrackers() {
        Block block = representation.rootBlock();
        HashMap<Block, BlockResultTracker> map = new HashMap<>();
        addTrackerForBlock(block, map);
        return Collections.unmodifiableMap(map);
    }

    private void addTrackerForBlock(Block block, Map<Block, BlockResultTracker> map) {
        if (representation.isLeaf(block)) {
            map.put(block, new LeafResultTracker());
        } else {
            representation.childrenOf(block).forEach(b -> addTrackerForBlock(b, map));
            List<Flux<Result>> childrenResults = representation.childrenOf(block).stream()
                    .map(map::get)
                    .map(BlockResultTracker::asStream).collect(toList());
            map.put(block, new BlockResultCombiner(childrenResults));
        }
    }

    @Override
    public void push(Block node, Result result) {
        if (!representation.isLeaf(node)) {
            throw new IllegalArgumentException("publishing results is only allowed for leaves.");
        }
        BlockResultTracker blockTracker = blockResultTrackers.get(node);
        if (blockTracker == null) {
            throw new IllegalStateException("No block tracker found for block '" + node + "'.");
        }
        if (blockTracker instanceof LeafResultTracker) {
            ((LeafResultTracker) blockTracker).push(result);
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
