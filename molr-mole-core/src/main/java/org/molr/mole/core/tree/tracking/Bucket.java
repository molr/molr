package org.molr.mole.core.tree.tracking;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.Result;

public interface Bucket<T> {

    void push(Block node, T result);

}
