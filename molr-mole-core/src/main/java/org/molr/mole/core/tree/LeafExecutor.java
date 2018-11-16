package org.molr.mole.core.tree;

import org.molr.commons.domain.*;
import org.molr.mole.core.tree.tracking.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.molr.commons.domain.Result.FAILED;
import static org.molr.commons.domain.Result.SUCCESS;
import static org.molr.commons.domain.RunState.FINISHED;
import static org.molr.commons.domain.RunState.RUNNING;

public abstract class LeafExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeafExecutor.class);

    private final Bucket<Result> resultBucket;
    private final Bucket<RunState> runStateBucket;
    private final MissionInput input;
    private final MissionOutputCollector output;

    protected LeafExecutor(Bucket<Result> resultBucket, Bucket<RunState> runStateBucket, MissionInput input, MissionOutputCollector output) {
        this.resultBucket = resultBucket;
        this.runStateBucket = runStateBucket;
        this.input = input;
        this.output = output;
    }


    protected MissionInput input() {
        return this.input;
    }

    protected BlockOutputCollector outputFor(Block block) {
        return new BlockOutputCollector(output, block);
    }

    public final Result execute(Block block) {
        runStateBucket.push(block, RUNNING);
        Result result = tryCatchExecute(block);
        resultBucket.push(block, result);
        runStateBucket.push(block, FINISHED);
        return result;
    }


    public final Result tryCatchExecute(Block block) {
        try {
            doExecute(block);
            return SUCCESS;
        } catch (Exception e) {
            LOGGER.warn("Execution of {} threw an exception: {}", block, e.getMessage(), e);
            return FAILED;
        }
    }


    protected abstract void doExecute(Block block);

}
