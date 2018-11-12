package org.molr.mole.core.tree.support;

import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.Result;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.tree.StrandExecutor;
import org.molr.mole.core.tree.tracking.TreeTracker;

import java.util.Set;

/**
 * Provides support methods for tests that act on one specific {@link StrandExecutor} and mission.
 * This condition makes it possible to reduce the overhead of parameters of the {@link StrandExecutorTestSupport}.
 */
public interface SingleMissionStrandExecutorTestSupport extends StrandExecutorTestSupport {

    StrandExecutor rootStrandExecutor();

    TreeTracker<Result> treeResultTracker();

    default void waitForRootStateToBe(RunState state) {
        waitForStrandStateToBe(rootStrandExecutor(), state);
    }

    default void waitForRootStrandToFinish() {
        waitForStrandToFinish(rootStrandExecutor());
    }

    default void waitForRootBlockToBe(Block block) {
        waitForActualBlockToBe(rootStrandExecutor(), block);
    }

    default void waitForResultOfBlockToBe(Block block, Result result) {
        waitForResultOfBlockToBe(treeResultTracker(), block, result);
    }

    default ObjectAssert<Block> assertThatRootBlock() {
        return assertThatActualBlockOf(rootStrandExecutor());
    }

    default AbstractComparableAssert<?, RunState> assertThatRootState() {
        return assertThatActualStateOf(rootStrandExecutor());
    }

    default AbstractComparableAssert<?, Result> assertThatResultOf(Block block) {
        return Assertions.assertThat(treeResultTracker().resultFor(block));
    }

    @Deprecated
    default void moveRootStrandTo(Block destination) {
        moveTo(rootStrandExecutor(), destination);
    }

    default StrandErrorsRecorder recordRootStrandErrors() {
        return recordStrandErrors(rootStrandExecutor());
    }

    @Deprecated
    default Set<StrandExecutor> rootChildrenStrandExecutors() {
        return childrenStrandExecutorsOf(rootStrandExecutor());
    }

    default void waitForProcessedCommandByRoot(StrandCommand command) {
        waitForProcessedCommand(rootStrandExecutor(), command);
    }

    /**
     * Will instruct the specified command on the {@link #rootStrandExecutor()} and wait for it to be processed
     */
    default void instructRootSync(StrandCommand command) {
        instructSync(rootStrandExecutor(), command);
    }

    /**
     * Will instruct the specified command on the {@link #rootStrandExecutor()} and return immediately
     */
    default void instructRootAsync(StrandCommand command) {
        instructAsync(rootStrandExecutor(), command);
    }

}
