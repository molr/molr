package org.molr.agency.core.support;

import org.molr.commons.api.Agent;
import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.StrandCommand;
import reactor.core.publisher.Mono;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Provides convenience methods for simple ways of running missions. It might have to be seen later, if some of them
 * should be moved later to the agency directly, especially if this move would prevent unnecessary network traffic.
 */
public class AgencyConvenienceSupport {

    private final Agent agent;

    public AgencyConvenienceSupport(Agent agent) {
        this.agent = requireNonNull(agent, "agency must not be null");
    }

    public OngoingMissionRun start(Mission mission, Map<String, Object> missionParameters) {
        Mono<MissionHandle> handle = agent.instantiate(mission, missionParameters);
        handle.subscribe(h -> agent.instructRoot(h, StrandCommand.RESUME));
        return new OngoingMissionRun(agent, handle);
    }

}
