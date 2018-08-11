/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.server.api;

import java.util.Map;

import org.molr.commons.api.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Agency {

    Flux<AgencyState> states();

    Flux<Mission> executableMissions();

    Mono<MissionDescription> representationOf(Mission mission);

    Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params);

    Flux<MissionState> statesFor(MissionHandle handle);

    void instruct(MissionHandle handle, MissionCommand command);

}
