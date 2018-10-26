package org.molr.mole.core.tree;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.MissionRepresentation;

import java.util.List;
import java.util.Optional;
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

    public Optional<Block> nextBlock(Block actualBlock) {
        Optional<Block> maybeParent = parentOf(actualBlock);
        if (!maybeParent.isPresent()) {
            /* Root block has no next */
            return Optional.empty();
        }
        Block parent = maybeParent.get();

        if (isLeaf(actualBlock)) {
            return nextBlock(parent);
        }

        if (isParallel(actualBlock)) {
            return nextBlock(parent);
        }

        List<Block> siblings = childrenOf(parent);
        if(isLastSibling(actualBlock, siblings)) {
            return nextBlock(parent);
        }

        return nextSiblingOf(actualBlock, siblings);
    }

    private boolean isLastSibling(Block actualBlock, List<Block> siblings) {
        return siblings.indexOf(actualBlock) >= siblings.size() - 1;
    }

    private Optional<Block> nextSiblingOf(Block block, List<Block> siblings) {
        if (isLastSibling(block, siblings)) {
            return Optional.empty();
        }
        return Optional.of(siblings.get(siblings.indexOf(block) + 1));
    }

    private Optional<Block> parentOf(Block block) {
        return representation.parentOf(block);
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
