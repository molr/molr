package io.molr.mole.core.support;


import io.molr.commons.domain.*;
import io.molr.mole.core.api.Mole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 *
 */
public class OngoingMissionRun {

    protected final Mole mole;
    protected final Mono<MissionHandle> handle;

    public OngoingMissionRun(Mole mole, Mono<MissionHandle> handle) {
        this.mole = requireNonNull(mole, "mole must not be null");
        this.handle = requireNonNull(handle, "handle must not be null");
    }

    /**
     * @return {@link OngoingMissionRun}
     */
    public OngoingMissionRun and() {
        return this;
    }

    public void forget() {
    }

    public Mono<Result> awaitFinished(Duration timeout) {
        return awaitFinished().timeout(timeout);
    }

    public Mono<Result> awaitFinished() {
        return handle
                .flatMapMany(mole::statesFor)
                .filter(s -> RunState.FINISHED.equals(s.runState()))
                .elementAt(0)
                .map(MissionState::result);
    }

    public Mono<MissionHandle> asyncHandle() {
        return handle;
    }

    public MissionHandle awaitHandle() {
        return handle.block();
    }

    public void instruct(StrandCommand command) {
        handle.subscribe(h -> mole.instructRoot(h, command));
    }

    public void await(RunStateValidator validator) {
        filterState(validator, handle, mole)
                .blockFirst();
    }

    public void await(RunStateValidator validator, Duration timeout) {
        filterState(validator, handle, mole)
                .blockFirst(timeout);
    }

    private static Flux<MissionState> filterState(RunStateValidator validator, Mono<MissionHandle> handle, Mole mole) {
        return handle
                .flatMapMany(mole::statesFor)
                .filter(s -> validator.test(s.runState()));
    }

    public ReturnHelper<Result> returnResult() {
        return new ReturnHelper<Result>(new ReturnResult());
    }

    public ReturnHelper<MissionState> returnState() {
        return new ReturnHelper<MissionState>(new ReturnState());
    }

    public static RunStateValidator runState(RunState runState) {
        return RunStateValidator.validateState(runState);
    }

    public class ReturnHelper<T> {
        private Function<MissionState, T> function;

        public ReturnHelper(Function<MissionState, T> function) {
            this.function = requireNonNull(function);
        }

        public T when(RunStateValidator validator) {
            return returnValue(filterState(validator, handle, mole).blockFirst());
        }

        public T when(RunStateValidator validator, Duration timeout) {
            return returnValue(filterState(validator, handle, mole).blockFirst(timeout));
        }

        public T whenFinished() {
            return when(runState(RunState.FINISHED));
        }

        public T whenFinished(Duration timeout) {
            return when(runState(RunState.FINISHED), timeout);
        }

        public T whenPaused() {
            return when(runState(RunState.PAUSED));
        }

        public T whenPaused(Duration timeout) {
            return when(runState(RunState.PAUSED), timeout);
        }

        private T returnValue(MissionState missionState) {
            return function.apply(missionState);
        }
    }

    private class ReturnState implements Function<MissionState, MissionState> {
        @Override
        public MissionState apply(MissionState missionState) {
            return missionState;
        }
    }

    private class ReturnResult implements Function<MissionState, Result> {
        @Override
        public Result apply(MissionState missionState) {
            return missionState.result();
        }
    }
}
