package io.molr.mole.core.tree.tracking;

import io.molr.commons.domain.Block;

public interface Bucket<T> {

    void push(Block node, T result);

}
