/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.mole.core.api;

import java.util.Map;
import java.util.Set;

import org.molr.commons.domain.Mission;
import org.molr.commons.domain.StrandCommand;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.MissionRepresentation;
import org.molr.commons.domain.MissionState;
import org.molr.commons.domain.Strand;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Mole {

    Set<Mission> availableMissions();

    Mono<MissionRepresentation> representationOf(Mission mission);

    void instantiate(MissionHandle handle, Mission mission, Map<String, Object> params);

    Flux<MissionState> statesFor(MissionHandle handle);

    void instruct(MissionHandle handle, Strand strand, StrandCommand command);

}
