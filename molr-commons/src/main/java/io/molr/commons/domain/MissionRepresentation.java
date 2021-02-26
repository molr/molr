/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package io.molr.commons.domain;

import com.google.common.collect.ListMultimap;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public interface MissionRepresentation {

    Block rootBlock();

    List<Block> childrenOf(Block block);

    Set<Block> allBlocks();

    boolean isLeaf(Block block);

    Optional<Block> parentOf(Block block);

    ListMultimap<Block, Block> parentsToChildren();

    default Optional<Block> blockOfId(String id) {
        return allBlocks().stream().filter(b -> Objects.equals(id, b.id())).findAny();
    }

    /**
     * Container to augment blocks with one or multiple attributes.
     * @return attribute lists mapped by blocks
     */
    ListMultimap<Block, BlockAttribute> blockAttributes();
}