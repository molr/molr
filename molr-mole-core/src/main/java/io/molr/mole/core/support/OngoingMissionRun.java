package io.molr.mole.core.support;


import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionState;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.api.Mole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class OngoingMissionRun {

    private final Mole mole;
    private final Mono<MissionHandle> handle;

    public OngoingMissionRun(Mole mole, Mono<MissionHandle> handle) {
        this.mole = requireNonNull(mole, "agency must not be null");
        this.handle = requireNonNull(handle, "handle must not be null");
    }

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

    public Result whenFinished(Duration timeout) {
        return awaitFinished().block(timeout);
    }

    public Result whenFinished() {
        return awaitFinished().block();
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

    public ReturnHelper returnResult() {
        return new ReturnHelper<MissionState, Result>(Result.class);
    }

    public ReturnHelper returnState() {
        return new ReturnHelper<MissionState, MissionState>(MissionState.class);
    }

    public static RunStateValidator runState(RunState runState) {
        return RunStateValidator.validateState(runState);
    }

    public class ReturnHelper<T, R> {
        private Function<T, R> function;

        public ReturnHelper(Class<R> type) {
            requireNonNull(type);
            if (MissionState.class.equals(type)) {
                function = new ReturnState<T, R>();
            } else if (Result.class.equals(type)) {
                function = new ReturnResult<T, R>();
            }
        }

        public R when(RunStateValidator validator) {
            return (R) returnValue(filterState(validator, handle, mole).blockFirst());
        }

        public R when(RunStateValidator validator, Duration timeout) {
            return (R) returnValue(filterState(validator, handle, mole).blockFirst(timeout));
        }

        public R whenFinished() {
            return when(runState(RunState.FINISHED));
        }

        public R whenFinished(Duration timeout) {
            return when(runState(RunState.FINISHED), timeout);
        }

        public R whenPaused() {
            return when(runState(RunState.PAUSED));
        }

        public R whenPaused(Duration timeout) {
            return when(runState(RunState.PAUSED), timeout);
        }

        private R returnValue(MissionState missionState) {
            return (R) function.apply((T) missionState);
        }
    }

    private class ReturnState<T, R> implements Function<T, R> {
        @Override
        public R apply(T missionState) {
            return (R) missionState;
        }
    }

    private class ReturnResult<T, R> implements Function<T, R> {
        @Override
        public R apply(T missionState) {
            return (R) ((MissionState) missionState).result();
        }
    }

}
