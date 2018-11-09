package org.molr.mole.core.tree;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.Result;

import java.util.Map;

public interface ResultTracker {

    Result resultFor(Block block);

    Map<Block, Result> blockResults();
}
