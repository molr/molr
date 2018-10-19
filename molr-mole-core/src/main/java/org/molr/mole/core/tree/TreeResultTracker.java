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
import java.util.stream.Collectors;

public class TreeResultTracker implements ResultBucket, ResultTracker {

    private final MissionRepresentation representation;

    private final Map<Block, BlockResultTracker> leafStreams;

    public TreeResultTracker(MissionRepresentation representation) {
        this.representation = representation;
        this.leafStreams = createBlockTrackers();
    }

    private Map<Block, BlockResultTracker> createBlockTrackers() {
        Block block = representation.rootBlock();
        HashMap<Block, BlockResultTracker> map = new HashMap<>();
        addTrackerForBlock(block, map);
        return Collections.unmodifiableMap(map);
    }

    private void addTrackerForBlock(Block block, Map<Block, BlockResultTracker> map) {
        if(representation.isLeaf(block)) {
            map.put(block, new LeafResultTracker());
        } else {
            representation.childrenOf(block).forEach(b -> addTrackerForBlock(b, map));
            List<Flux<Result>> childrenResults = representation.childrenOf(block).stream().map(map::get).map(BlockResultTracker::asStream).collect(Collectors.toList());
            map.put(block, new BlockResultCombiner(childrenResults));
        }
    }

    @Override
    public void push(Block node, Result result) {
        if (!representation.isLeaf(node)) {
            throw new IllegalArgumentException("publishing results is only allowed for leaves.");
        }
        BlockResultTracker blockTracker = leafStreams.get(node);
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
        return leafStreams.get(block).result();
    }
}
