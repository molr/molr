package io.molr.mole.core.support;


import io.molr.commons.domain.*;
import io.molr.mole.core.api.Mole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

import static io.molr.mole.core.support.MissionPredicates.runStatePredicate;
import static java.util.Objects.requireNonNull;

/**
 * Provides capabilities to control an ongoing mission run
 */
public class OngoingMissionRun {

    private final Mole mole;
    private final Mono<MissionHandle> handle;

    /**
     * @param mole   the {@link Mole} which has missions registered
     * @param handle the {@link MissionHandle} of running {@link Mission}
     */
    public OngoingMissionRun(Mole mole, Mono<MissionHandle> handle) {
        this.mole = requireNonNull(mole, "mole must not be null");
        this.handle = requireNonNull(handle, "handle must not be null");
    }

    /**
     * Gives meaning to method chaining when chaining two unrelated methods
     *
     * @return an instance of {@link OngoingMissionRun}
     */
    public OngoingMissionRun and() {
        return this;
    }

    /**
     * Gives meaning to method chaining when the user just wants to trigger a {@link Mission} and does not want control
     */
    public void forget() {
    }

    protected Mole mole() {
        return mole;
    }

    /**
     * @param timeout the timeout before onNext signal
     * @return a {@link Mono} of {@link Result}
     */
    public Mono<Result> awaitFinished(Duration timeout) {
        return awaitFinished().timeout(timeout);
    }

    /**
     * @return a {@link Mono} of {@link Result}
     */
    public Mono<Result> awaitFinished() {
        return handle
                .flatMapMany(mole::statesFor)
                .filter(s -> RunState.FINISHED.equals(s.runState()))
                .elementAt(0)
                .map(MissionState::result);
    }

    /**
     * @return a {@link Mono} of {@link MissionHandle}
     */
    public Mono<MissionHandle> asyncHandle() {
        return handle;
    }

    /**
     * @return the {@link MissionHandle} represented by this {@link OngoingMissionRun}
     */
    public MissionHandle awaitHandle() {
        return handle.block();
    }

    /**
     * @param command the {@link StrandCommand} to be given to running {@link Mission}
     */
    public void instruct(StrandCommand command) {
        handle.subscribe(h -> mole.instructRoot(h, command));
    }

    /**
     * @param runStateValidator a {@link MissionPredicates} for {@link RunState}
     */
    public void await(MissionPredicates<RunState> runStateValidator) {
        filterState(runStateValidator, handle, mole)
                .blockFirst();
    }

    /**
     * @param runStateValidator a {@link MissionPredicates} for {@link RunState}
     * @param timeout           the timeout before onNext signal
     */
    public void await(MissionPredicates<RunState> runStateValidator, Duration timeout) {
        filterState(runStateValidator, handle, mole)
                .blockFirst(timeout);
    }

    /**
     * Filters the {@link MissionState}s matching the {@link RunState} {@link MissionPredicates}
     *
     * @param runStateValidator a {@link MissionPredicates} for {@link RunState}
     * @param handle            the {@link MissionHandle} of running {@link Mission}
     * @param mole              the {@link Mole} which has missions registered
     * @return a {@link Flux} of {@link MissionState}
     */
    private static Flux<MissionState> filterState(MissionPredicates<RunState> runStateValidator,
                                                  Mono<MissionHandle> handle, Mole mole) {
        return handle
                .flatMapMany(mole::statesFor)
                .filter(s -> runStateValidator.test(s.runState()));
    }

    /**
     * @return the {@link ReturnHelper} to get {@link Result}
     */
    public ReturnHelper<Result> returnResult() {
        return new ReturnHelper<Result>(new ReturnResult());
    }

    /**
     * @return the {@link ReturnHelper} to get {@link MissionState}
     */
    public ReturnHelper<MissionState> returnState() {
        return new ReturnHelper<MissionState>(new ReturnState());
    }

    /**
     * A helper class to filter {@link MissionState} based on {@link MissionPredicates} of {@link RunState} provided
     *
     * @param <T> the type of value to be returned
     */
    public class ReturnHelper<T> {
        private Function<MissionState, T> function;

        /**
         * @param function the {@link Function} to apply on {@link MissionState}
         */
        public ReturnHelper(Function<MissionState, T> function) {
            this.function = requireNonNull(function);
        }

        /**
         * Returns the output of {@link Function} applied on {@link MissionState} filtered from {@link RunState}
         * {@link MissionPredicates}
         *
         * @param runStateValidator a {@link MissionPredicates} for {@link RunState}
         * @return the return value {@link T}
         */
        public T when(MissionPredicates<RunState> runStateValidator) {
            return returnValue(filterState(runStateValidator, handle, mole).blockFirst());
        }

        /**
         * Returns the output of {@link Function} applied on {@link MissionState} filtered from {@link RunState}
         * {@link MissionPredicates} with a timeout
         *
         * @param runStateValidator a {@link MissionPredicates} for {@link RunState}
         * @param timeout
         * @return the return value {@link T}
         */
        public T when(MissionPredicates<RunState> runStateValidator, Duration timeout) {
            return returnValue(filterState(runStateValidator, handle, mole).blockFirst(timeout));
        }

        /**
         * Returns the output of {@link Function} applied on {@link MissionState} filtered when {@link RunState} is
         * FINISHED
         *
         * @return the return value {@link T}
         */
        public T whenFinished() {
            return when(runStatePredicate(RunState.FINISHED));
        }

        /**
         * Returns the output of {@link Function} applied on {@link MissionState} filtered when {@link RunState} is
         * FINISHED with a timeout
         *
         * @param timeout
         * @return the return value {@link T}
         */
        public T whenFinished(Duration timeout) {
            return when(runStatePredicate(RunState.FINISHED), timeout);
        }

        /**
         * Returns the output of {@link Function} applied on {@link MissionState} filtered when {@link RunState} is
         * PAUSED
         *
         * @return the return value {@link T}
         */
        public T whenPaused() {
            return when(runStatePredicate(RunState.PAUSED));
        }

        /**
         * Returns the output of {@link Function} applied on {@link MissionState} filtered when {@link RunState} is
         * PAUSED with a timeout
         *
         * @param timeout
         * @return the return value {@link T}
         */
        public T whenPaused(Duration timeout) {
            return when(runStatePredicate(RunState.PAUSED), timeout);
        }

        /**
         * Returns the output of {@link Function} applied on {@link MissionState}
         *
         * @param missionState the {@link MissionState}
         * @return the return value {@link T}
         */
        private T returnValue(MissionState missionState) {
            return function.apply(missionState);
        }
    }

    /**
     * A function that returns the same {@link MissionState}
     */
    private class ReturnState implements Function<MissionState, MissionState> {
        @Override
        public MissionState apply(MissionState missionState) {
            return missionState;
        }
    }

    /**
     * A functions that returns the {@link Result} from {@link MissionState}
     */
    private class ReturnResult implements Function<MissionState, Result> {
        @Override
        public Result apply(MissionState missionState) {
            return missionState.result();
        }
    }
}
