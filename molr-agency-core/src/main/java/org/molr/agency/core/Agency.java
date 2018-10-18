/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.agency.core;

import java.util.Map;

import org.molr.commons.domain.AgencyState;
import org.molr.commons.domain.Mission;
import org.molr.commons.domain.StrandCommand;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.MissionRepresentation;
import org.molr.commons.domain.MissionState;
import org.molr.commons.domain.Strand;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Agency {

    Flux<AgencyState> states();

    Mono<MissionRepresentation> representationOf(Mission mission);

    Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params);

    Flux<MissionState> statesFor(MissionHandle handle);

    void instruct(MissionHandle handle, Strand strand, StrandCommand command);

}
