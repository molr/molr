package org.molr.mole.core.tree.tracking;

import org.molr.commons.domain.Block;

public interface Bucket<T> {

    void push(Block node, T result);

}
