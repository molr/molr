package io.molr.mole.core.tree.tracking;

import java.util.Map;

import io.molr.commons.domain.Block;

public interface Tracker<T> {

    T resultFor(Block block);

    Map<Block, T> blockResults();
}
