package org.molr.agency.core;

import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.Result;
import org.molr.commons.domain.StrandCommand;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Provides convenience methods for simple ways of running missions.
 */
public class MissionRunningSupport {

    private final Agency agency;

    public MissionRunningSupport(Agency agency) {
        this.agency = requireNonNull(agency, "agency must not be null");
    }

    public Mono<MissionHandle> start(Mission mission, Map<String, Object> missionParameters) {
        Mono<MissionHandle> handle = agency.instantiate(mission, missionParameters);
        handle.subscribe(h -> agency.instructRoot(h, StrandCommand.RESUME));
        return handle;
    }

    public Mono<Result> awaitFinished(MissionHandle handle) {
        return null;
    }

    public Mono<Result> run(Mission mission, Map<String, Object> missionParameters) {
        return start(mission, missionParameters).flatMap(this::awaitFinished);
    }

}
