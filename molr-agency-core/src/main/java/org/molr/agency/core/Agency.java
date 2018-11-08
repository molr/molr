/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.agency.core;

import java.util.Map;

import org.molr.commons.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Agency {

    Flux<AgencyState> states();

    Mono<MissionRepresentation> representationOf(Mission mission);

    Mono<MissionParameterDescription> parameterDescriptionOf(Mission mission);

    Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params);

    Flux<MissionState> statesFor(MissionHandle handle);

    Flux<MissionOutput> outputsFor(MissionHandle handle);

    void instruct(MissionHandle handle, Strand strand, StrandCommand command);

}
