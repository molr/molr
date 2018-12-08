package io.molr.mole.core.tree.tracking;

import io.molr.commons.domain.Block;

import java.util.Map;

public interface Tracker<T> {

    T resultFor(Block block);

    Map<Block, T> blockResults();
}
