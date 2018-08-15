/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.api.domain;

import java.util.List;
import java.util.Set;

public interface MissionRepresentation {

    Block rootBlock();

    List<Block> childrenOf(Block block);

    Set<Block> allBlocks();

}