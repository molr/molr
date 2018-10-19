package org.molr.mole.core.tree;

import org.molr.commons.domain.Block;

public abstract class LeafExecutor {

    private final ResultBucket resultTracker;

    protected LeafExecutor(ResultBucket resultTracker) {
        this.resultTracker = resultTracker;
    }

    public abstract void execute(Block block);

    protected ResultBucket tracker() {
        return this.resultTracker;
    }


}
