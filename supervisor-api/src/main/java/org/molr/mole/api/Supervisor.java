/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.mole.api;

import java.util.Map;
import java.util.Set;

import org.molr.commons.api.domain.Mission;
import org.molr.commons.api.domain.MissionCommand;
import org.molr.commons.api.domain.MissionRepresentation;
import org.molr.commons.api.domain.MissionHandle;
import org.molr.commons.api.domain.MissionState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Supervisor {

    Set<Mission> availableMissions();

    Mono<MissionRepresentation> representationOf(Mission mission);

    void instantiate(MissionHandle handle, Mission mission, Map<String, Object> params);

    Flux<MissionState> statesFor(MissionHandle handle);

    void instruct(MissionHandle handle, MissionCommand command);

}
