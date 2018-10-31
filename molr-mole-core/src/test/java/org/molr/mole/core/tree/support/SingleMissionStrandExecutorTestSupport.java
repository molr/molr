package org.molr.mole.core.tree.support;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.Result;
import org.molr.commons.domain.RunState;
import org.molr.mole.core.tree.StrandExecutor;
import org.molr.mole.core.tree.TreeResultTracker;

/**
 * Provides support methods for tests that act on one specific {@link StrandExecutor} and mission.
 * This condition makes it possible to reduce the overhead of parameters of the {@link StrandExecutorTestSupport}.
 */
public interface SingleMissionStrandExecutorTestSupport extends StrandExecutorTestSupport {

    StrandExecutor strandExecutor();

    TreeResultTracker treeResultTracker();

    default RunState waitForStateToBe(RunState state) {
        return waitForStateToBe(strandExecutor(), state);
    }

    default boolean isFinishedSync() {
        return isFinishedSync(strandExecutor());
    }

    default void waitForActualBlockToBe(Block block) {
        waitForActualBlockToBe(strandExecutor(), block);
    }

    default void waitForResultOfBlockToBe(Block block, Result result) {
        waitForResultOfBlockToBe(treeResultTracker(), block, result);
    }

    @Deprecated
    default void moveTo(Block destination) {
        moveTo(strandExecutor(), destination);
    }

}
