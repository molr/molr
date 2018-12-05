package org.molr.mole.core.support;

import org.molr.commons.api.Mole;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.MissionState;
import org.molr.commons.domain.Result;
import org.molr.commons.domain.RunState;
import reactor.core.publisher.Mono;

import java.time.Duration;

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

    public Mono<MissionHandle> forget() {
        return this.handle;
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

}
