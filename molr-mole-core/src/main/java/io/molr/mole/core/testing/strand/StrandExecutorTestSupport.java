package io.molr.mole.core.testing.strand;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.tree.StrandExecutor;
import io.molr.mole.core.tree.executor.ConcurrentStrandExecutorStacked;
import io.molr.mole.core.tree.tracking.TreeTracker;
import org.assertj.core.api.*;

import java.time.Duration;
import java.util.Set;

/**
 * Provides support default methods for testing strand executor behaviour
 */
public interface StrandExecutorTestSupport {

    Duration TIMEOUT = Duration.ofSeconds(30);

    default void waitUntilStrandStateIs(StrandExecutor strandExecutor, RunState state) {
    	System.out.println(strandExecutor+"subscirbe\n\n\n");
        strandExecutor.getStateStream().filter(streamedState->{
        	System.out.println("sub:"+state);
        	return state.equals(streamedState);
        }).blockFirst(TIMEOUT);
        assertThatStateOf(strandExecutor).isEqualTo(state);
    }

    default void waitUntilStrandIsFinished(StrandExecutor strandExecutor) {
        waitUntilStrandStateIs(strandExecutor, RunState.FINISHED);
        assertThatStateOf(strandExecutor).isEqualTo(RunState.FINISHED);
    }

    default void waitUntilStrandBlockIs(StrandExecutor strandExecutor, Block block) {
        strandExecutor.getBlockStream().filter(block::equals).blockFirst(TIMEOUT);
        assertThatBlockOf(strandExecutor).isEqualTo(block);
    }

    default void waitUntilResultOfBlockIs(TreeTracker<Result> resultTracker, Block block, Result result) {
        resultTracker.resultUpdatesFor(block).filter(result::equals).blockFirst(TIMEOUT);
        assertThatResultOf(resultTracker, block).isEqualTo(result);
    }

    default void waitForProcessedCommand(StrandExecutor strandExecutor, StrandCommand command, long id) {
        ((ConcurrentStrandExecutorStacked) strandExecutor).getLastCommandStream()
                .filter(cmd -> {
                	System.out.println("filterFun"+id+" "+cmd.getCommandId());
                	return cmd.getCommandId() == id;
                }).blockFirst(TIMEOUT);
    }

    default void waitForErrorOfType(StrandErrorsRecorder recorder, Class<? extends Exception> clazz) {
        recorder.getRecordedExceptionStream().any(exceptions -> exceptions.stream().anyMatch(clazz::isInstance)).block(TIMEOUT);
    }

    default AbstractComparableAssert<?, Result> assertThatResultOf(TreeTracker<Result> resultTracker, Block block) {
        return Assertions.assertThat(resultTracker.resultFor(block));
    }

    default ObjectAssert<Block> assertThatBlockOf(StrandExecutor strandExecutor) {
        return Assertions.assertThat(strandExecutor.getActualBlock());
    }

    default AbstractComparableAssert<?, RunState> assertThatStateOf(StrandExecutor executor) {
        return Assertions.assertThat(executor.getActualState());
    }

    default ListAssert<Exception> assertThat(StrandErrorsRecorder recorder) {
        return Assertions.assertThat(recorder.getExceptions());
    }

    default IterableAssert<StrandCommand> assertThatAllowedCommandsOf(StrandExecutor executor) {
        return Assertions.assertThat(executor.getAllowedCommands());
    }

    default StrandErrorsRecorder recordStrandErrors(StrandExecutor executor) {
        return new StrandErrorsRecorder(executor);
    }

    default Set<StrandExecutor> childrenStrandExecutorsOf(StrandExecutor executor) {
        return ((ConcurrentStrandExecutorStacked) executor).getChildrenStrandExecutors();
    }

    /**
     * Will instruct the specified command on the specified {@link StrandExecutor} and wait for it to be processed
     * processing
     */
    default void instructSync(StrandExecutor executor, StrandCommand command) {
        long id = executor.instruct(command);
        waitForProcessedCommand(executor, command, id);
    }

    /**
     * Will instruct the specified command on the specified {@link StrandExecutor} and return immediately
     */
    default void instructAsync(StrandExecutor executor, StrandCommand command) {
        executor.instruct(command);
    }

//    @Deprecated
//    default void moveTo(StrandExecutor executor, Block destination) {
//        ((ConcurrentStrandExecutor) executor).moveTo(destination);
//        assertThatBlockOf(executor).isEqualTo(destination);
//    }

}
