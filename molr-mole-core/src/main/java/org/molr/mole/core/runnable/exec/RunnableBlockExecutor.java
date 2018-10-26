package org.molr.mole.core.runnable.exec;

import com.google.common.collect.ImmutableMap;
import org.molr.commons.domain.Block;
import org.molr.mole.core.tree.LeafExecutor;
import org.molr.mole.core.tree.ResultBucket;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.molr.commons.domain.Result.FAILED;
import static org.molr.commons.domain.Result.SUCCESS;

public class RunnableBlockExecutor extends LeafExecutor {

    private final Map<Block, Runnable> runnables;

    public RunnableBlockExecutor(ResultBucket resultTracker, Map<Block, Runnable> runnables) {
        super(resultTracker);
        this.runnables = ImmutableMap.copyOf(runnables);
    }

    @Override
    public void execute(Block block) {
        try {
            runnables.get(block).run();
            tracker().push(block, SUCCESS);
        } catch (Exception e) {
            tracker().push(block, FAILED);
        }
    }

    /**
     * VERY SIMPLISTIC IMPLEMENTATION FOR NOW TODO Implement properly
     */
    @Deprecated
    @Override
    public CompletableFuture<Boolean> executeAsync(Block block) {
        try {
            execute(block);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }
    }

}
