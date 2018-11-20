package org.molr.mole.core.tree.tracking;

import org.molr.commons.domain.Block;

import java.util.Map;

public interface Tracker<T> {

    T resultFor(Block block);

    Map<Block, T> blockResults();
}
