package org.molr.mole.core.tree.support;

import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
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

    default void waitForStrandStateToBe(StrandExecutor strandExecutor, RunState state) {
        strandExecutor.getStateStream().filter(state::equals).blockFirst(Duration.ofMinutes(1));
    }

    default void waitForStrandToFinish(StrandExecutor strandExecutor) {
        waitForStrandStateToBe(strandExecutor, RunState.FINISHED);
    }

    default void waitForActualBlockToBe(StrandExecutor strandExecutor, Block block) {
        strandExecutor.getBlockStream().filter(block::equals).blockFirst(Duration.ofMinutes(1));
    }

    default void waitForResultOfBlockToBe(TreeResultTracker resultTracker, Block block, Result result) {
        resultTracker.resultUpdatesFor(block).filter(result::equals).blockFirst(Duration.ofMinutes(1));
    }

    default ObjectAssert<Block> assertThatActualBlockOf(StrandExecutor strandExecutor) {
        return Assertions.assertThat(strandExecutor.getActualBlock());
    }

    default AbstractComparableAssert<?, RunState> assertThatActualStateOf(StrandExecutor executor) {
        return Assertions.assertThat(executor.getActualState());
    }

    @Deprecated
    default void moveTo(StrandExecutor executor, Block destination) {
        ((ConcurrentStrandExecutor) executor).moveTo(destination);
    }
}
