package org.molr.mole.core.tree.support;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.Result;
import org.molr.commons.domain.RunState;
import org.molr.mole.core.tree.ConcurrentStrandExecutor;
import org.molr.mole.core.tree.StrandExecutor;
import org.molr.mole.core.tree.TreeResultTracker;

import java.time.Duration;

/**
 * Provides support default methods for testing strand executor behaviour
 */
public interface StrandExecutorTestSupport {

    default RunState waitForStateToBe(StrandExecutor strandExecutor, RunState state) {
        return strandExecutor.getStateStream().filter(state::equals).blockFirst(Duration.ofMinutes(1));
    }

    default boolean isFinishedSync(StrandExecutor strandExecutor) {
        return waitForStateToBe(strandExecutor, RunState.FINISHED) != null;
    }

    default void waitForActualBlockToBe(StrandExecutor strandExecutor, Block block) {
        strandExecutor.getBlockStream().filter(block::equals).blockFirst(Duration.ofMinutes(1));
    }

    default void waitForResultOfBlockToBe(TreeResultTracker resultTracker, Block block, Result result) {
        resultTracker.resultUpdatesFor(block).filter(result::equals).blockFirst(Duration.ofMinutes(1));
    }

    @Deprecated
    default void moveTo(StrandExecutor executor, Block destination) {
        ((ConcurrentStrandExecutor) executor).moveTo(destination);
    }
}
