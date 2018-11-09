package org.molr.mole.core.tree.support;

import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.Result;
import org.molr.commons.domain.RunState;
import org.molr.mole.core.tree.ConcurrentStrandExecutor;
import org.molr.mole.core.tree.StrandExecutor;
import org.molr.mole.core.tree.tracking.TreeTracker;

import java.time.Duration;
import java.util.Set;

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

    default void waitForResultOfBlockToBe(TreeTracker resultTracker, Block block, Result result) {
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

    default void waitForErrorOfType(StrandErrorsRecorder recorder, Class<? extends Exception> clazz) {
        recorder.getRecordedExceptionStream().any(exceptions -> exceptions.stream().anyMatch(clazz::isInstance)).block(Duration.ofMinutes(1));
    }

    default ListAssert<Exception> assertThat(StrandErrorsRecorder recorder) {
        return Assertions.assertThat(recorder.getExceptions());
    }

    default StrandErrorsRecorder recordStrandErrors(StrandExecutor executor) {
        return new StrandErrorsRecorder(executor);
    }

    @Deprecated
    default Set<StrandExecutor> childrenStrandExecutorsOf(StrandExecutor executor) {
        return executor.getChildrenStrandExecutors();
    }

}
