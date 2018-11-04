package org.molr.mole.core.tree;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.Result;

public abstract class LeafExecutor {

    private final ResultBucket resultBucket;

    protected LeafExecutor(ResultBucket resultBucket) {
        this.resultBucket = resultBucket;
    }

    protected ResultBucket resultBucket() {
        return this.resultBucket;
    }

    public abstract Result execute(Block block);

}
