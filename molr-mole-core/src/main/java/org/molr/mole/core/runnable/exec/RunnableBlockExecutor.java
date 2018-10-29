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
    public boolean execute(Block block) {
        try {
//            if(block.id().equals("9"))
//                throw new RuntimeException("SIMULATION");
            Thread.sleep(1000);
            runnables.get(block).run();
            tracker().push(block, SUCCESS);
            return true;
        } catch (Exception e) {
            tracker().push(block, FAILED);
            return false;
        }
    }

    /**
     * VERY SIMPLISTIC IMPLEMENTATION FOR NOW TODO Implement properly
     */
    @Deprecated
    @Override
    public CompletableFuture<Boolean> executeAsync(Block block) {
        return CompletableFuture.supplyAsync(() -> execute(block));
    }

}
