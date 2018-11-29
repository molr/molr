package org.molr.agency.core.support;

import org.molr.agency.core.Agency;
import org.molr.commons.domain.*;
import reactor.core.publisher.Mono;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Provides convenience methods for simple ways of running missions. It might have to be seen later, if some of them
 * should be moved later to the agency directly, especially if this move would prevent unnecessary network traffic.
 */
public class AgencyConvenienceSupport {

    private final Agency agency;

    public AgencyConvenienceSupport(Agency agency) {
        this.agency = requireNonNull(agency, "agency must not be null");
    }

    public OngoingMissionRun start(Mission mission, Map<String, Object> missionParameters) {
        Mono<MissionHandle> handle = agency.instantiate(mission, missionParameters);
        handle.subscribe(h -> agency.instructRoot(h, StrandCommand.RESUME));
        return new OngoingMissionRun(agency, handle);
    }

}
