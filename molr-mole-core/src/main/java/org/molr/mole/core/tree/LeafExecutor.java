package org.molr.mole.core.tree;

import org.molr.commons.domain.*;
import org.molr.mole.core.tree.tracking.Bucket;

public abstract class LeafExecutor {

    private final Bucket resultBucket;
    private final MissionInput input;
    private final MissionOutputCollector output;

    protected LeafExecutor(Bucket resultBucket, MissionInput input, MissionOutputCollector output) {
        this.resultBucket = resultBucket;
        this.input = input;
        this.output = output;
    }

    protected Bucket resultBucket() {
        return this.resultBucket;
    }

    protected MissionInput input() {
        return this.input;
    }

    protected BlockOutputCollector outputFor(Block block) {
        return new BlockOutputCollector(output, block);
    }

    public abstract Result execute(Block block);

}
