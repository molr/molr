package org.molr.mole.core.tree;

import jdk.nashorn.internal.runtime.regexp.joni.WarnCallback;
import org.molr.commons.domain.*;
import org.molr.mole.core.tree.tracking.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.molr.commons.domain.Result.FAILED;
import static org.molr.commons.domain.Result.SUCCESS;

public abstract class LeafExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeafExecutor.class);

    private final Bucket<Result> resultBucket;
    private final MissionInput input;
    private final MissionOutputCollector output;

    protected LeafExecutor(Bucket<Result> resultBucket, MissionInput input, MissionOutputCollector output) {
        this.resultBucket = resultBucket;
        this.input = input;
        this.output = output;
    }

    protected Bucket<Result> resultBucket() {
        return this.resultBucket;
    }

    protected MissionInput input() {
        return this.input;
    }

    protected BlockOutputCollector outputFor(Block block) {
        return new BlockOutputCollector(output, block);
    }

    public final Result execute(Block block) {
        try {
            doExecute(block);
            resultBucket().push(block, SUCCESS);
            return SUCCESS;
        } catch (Exception e) {
            LOGGER.warn("Execution of {} threw an exception: {}", block, e.getMessage(), e);
            resultBucket().push(block, FAILED);
            return FAILED;
        }
    }

    protected abstract void doExecute(Block block);

}
