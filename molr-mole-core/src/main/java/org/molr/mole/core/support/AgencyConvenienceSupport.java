package org.molr.mole.core.support;

import org.molr.commons.api.Mole;
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

    private final Mole mole;

    public AgencyConvenienceSupport(Mole mole) {
        this.mole = requireNonNull(mole, "agency must not be null");
    }

    public OngoingMissionRun start(Mission mission, Map<String, Object> missionParameters) {
        Mono<MissionHandle> handle = mole.instantiate(mission, missionParameters);
        handle.subscribe(h -> mole.instructRoot(h, StrandCommand.RESUME));
        return new OngoingMissionRun(mole, handle);
    }

}
