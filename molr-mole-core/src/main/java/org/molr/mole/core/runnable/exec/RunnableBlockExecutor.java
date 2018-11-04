package org.molr.mole.core.runnable.exec;

import com.google.common.collect.ImmutableMap;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.Result;
import org.molr.mole.core.tree.LeafExecutor;
import org.molr.mole.core.tree.ResultBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.molr.commons.domain.Result.FAILED;
import static org.molr.commons.domain.Result.SUCCESS;

/**
 * Leaf executor that relates {@link Runnable} with {@link Block} for the leaf execution logic.
 * <p>
 * The result of a leaf is considered {@link Result#SUCCESS} if the execution does not throw any exception.
 */
public class RunnableBlockExecutor extends LeafExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnableBlockExecutor.class);

    private final Map<Block, Runnable> runnables;

    public RunnableBlockExecutor(ResultBucket resultTracker, Map<Block, Runnable> runnables) {
        super(resultTracker);
        this.runnables = ImmutableMap.copyOf(runnables);
    }

    @Override
    public Result execute(Block block) {
        try {
            runnables.get(block).run();
            resultBucket().push(block, SUCCESS);
            return SUCCESS;
        } catch (Exception e) {
            LOGGER.trace("Execution of {} threw an exception: {}", block, e.getMessage(), e);
            resultBucket().push(block, FAILED);
            return FAILED;
        }
    }

}
