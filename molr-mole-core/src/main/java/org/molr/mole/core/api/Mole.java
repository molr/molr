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

    /**
     * Has to return a string identifying the instance. It is sufficient that this id is unique within the same
     * implementations of this interface.
     *
     * @return an unique id for the mole
     */
    default String uid() {
        return "" + hashCode();
    }

    Set<Mission> availableMissions();

    MissionRepresentation representationOf(Mission mission);

    MissionParameterDescription parameterDescriptionOf(Mission mission);

    void instantiate(MissionHandle handle, Mission mission, Map<String, Object> params);

    Flux<MissionState> statesFor(MissionHandle handle);

    Flux<MissionOutput> outputsFor(MissionHandle handle);

    Flux<MissionRepresentation> representationsFor(MissionHandle handle);

    void instruct(MissionHandle handle, Strand strand, StrandCommand command);

    void instructRoot(MissionHandle handle, StrandCommand command);

}
