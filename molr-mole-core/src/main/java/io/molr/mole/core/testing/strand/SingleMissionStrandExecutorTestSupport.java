package io.molr.mole.core.testing.strand;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.tree.StrandExecutor;
import io.molr.mole.core.tree.tracking.TreeTracker;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ObjectAssert;

import java.util.Set;

/**
 * Provides support methods for tests that act on one specific {@link StrandExecutor} and mission. This condition makes
 * it possible to reduce the overhead of parameters of the {@link StrandExecutorTestSupport}.
 */
public interface SingleMissionStrandExecutorTestSupport extends StrandExecutorTestSupport {

    StrandExecutor rootStrandExecutor();

    TreeTracker<Result> treeResultTracker();

    default void waitUntilRootStrandStateIs(RunState state) {
        waitUntilStrandStateIs(rootStrandExecutor(), state);
    }

    default void waitUntilRootStrandIsFinished() {
        waitUntilStrandIsFinished(rootStrandExecutor());
    }

    default void waitUntilRootStrandBlockIs(Block block) {
        waitUntilStrandBlockIs(rootStrandExecutor(), block);
    }

    default void waitUntilResultOfBlockIs(Block block, Result result) {
        waitUntilResultOfBlockIs(treeResultTracker(), block, result);
    }

    default ObjectAssert<Block> assertThatRootStrandBlock() {
        return assertThatBlockOf(rootStrandExecutor());
    }

    default AbstractComparableAssert<?, RunState> assertThatRootStrandState() {
        return assertThatStateOf(rootStrandExecutor());
    }

    default AbstractComparableAssert<?, Result> assertThatResultOf(Block block) {
        return assertThatResultOf(treeResultTracker(), block);
    }

    default IterableAssert<StrandCommand> assertThatStrandRootAllowedCommands() {
        return assertThatAllowedCommandsOf(rootStrandExecutor());
    }

    @Deprecated
    default void moveRootStrandTo(Block destination) {
        moveTo(rootStrandExecutor(), destination);
    }

    default StrandErrorsRecorder recordRootStrandErrors() {
        return recordStrandErrors(rootStrandExecutor());
    }

    @Deprecated
    default Set<StrandExecutor> rootStrandChildren() {
        return childrenStrandExecutorsOf(rootStrandExecutor());
    }

    /**
     * Will instruct the specified command on the {@link #rootStrandExecutor()} and wait for it to be processed
     */
    default void instructRootStrandSync(StrandCommand command) {
        instructSync(rootStrandExecutor(), command);
    }

    /**
     * Will instruct the specified command on the {@link #rootStrandExecutor()} and return immediately
     */
    default void instructRootStrandAsync(StrandCommand command) {
        instructAsync(rootStrandExecutor(), command);
    }

}
