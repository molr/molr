package org.molr.mole.core.tree;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.MissionRepresentation;

import java.util.List;
import java.util.Set;

public class TreeStructure {

    private final MissionRepresentation representation;
    private final Set<Block> parallelBlocks;

    public TreeStructure(MissionRepresentation representation, Set<Block> parallelBlocks) {
        this.representation = representation;
        this.parallelBlocks = parallelBlocks;
    }

    public MissionRepresentation missionRepresentation() {
        return representation;
    }


    public List<Block> childrenOf(Block block) {
        return representation.childrenOf(block);
    }

    public boolean isParallel(Block block) {
        return parallelBlocks.contains(block);
    }

    public boolean isLeaf(Block block) {
        return representation.isLeaf(block);
    }

    public Block rootBlock() {
        return representation.rootBlock();
    }
}
