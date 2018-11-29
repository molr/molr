package org.molr.agency.core.support;

import org.molr.agency.core.Agency;
import org.molr.commons.domain.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class OngoingMissionRun {

    private final Agency agency;
    private final Mono<MissionHandle> handle;

    public OngoingMissionRun(Agency agency, Mono<MissionHandle> handle) {
        this.agency = requireNonNull(agency, "agency must not be null");
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
                .flatMapMany(agency::statesFor)
                .filter(s -> RunState.FINISHED.equals(s.runState()))
                .elementAt(0)
                .map(MissionState::result);
    }

}
