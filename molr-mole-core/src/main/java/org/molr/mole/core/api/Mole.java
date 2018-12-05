/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.mole.core.api;

import org.molr.commons.api.Agent;
import org.molr.commons.domain.*;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Set;

public interface Mole extends Agent {

    Set<Mission> availableMissions();

    MissionRepresentation representationOf(Mission mission);

    MissionParameterDescription parameterDescriptionOf(Mission mission);

    Flux<MissionState> statesFor(MissionHandle handle);

    Flux<MissionOutput> outputsFor(MissionHandle handle);

    Flux<MissionRepresentation> representationsFor(MissionHandle handle);

    void instruct(MissionHandle handle, Strand strand, StrandCommand command);

    void instructRoot(MissionHandle handle, StrandCommand command);

}
