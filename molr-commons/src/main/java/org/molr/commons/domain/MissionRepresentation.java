/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MissionRepresentation {

    Block rootBlock();

    List<Block> childrenOf(Block block);

    Set<Block> allBlocks();

    boolean isLeaf(Block block);

    Optional<Block> parentOf(Block block);
}