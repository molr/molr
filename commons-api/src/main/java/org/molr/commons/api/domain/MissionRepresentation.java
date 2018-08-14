/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.api.domain;

import java.util.List;

public interface MissionRepresentation {

    Mission mission();

    Block rootBlock();

    List<Block> childrenOf(Block block);

}