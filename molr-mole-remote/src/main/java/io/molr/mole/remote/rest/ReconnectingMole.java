package io.molr.mole.remote.rest;

import java.util.Map;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.AgencyState;
import io.molr.commons.domain.BlockCommand;
import io.molr.commons.domain.ImmutableAgencyState;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionCommand;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionOutput;
import io.molr.commons.domain.MissionParameterDescription;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.MissionState;
import io.molr.commons.domain.Strand;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.api.Mole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReconnectingMole implements Mole{

    private Mole remoteMole;

    Flux<AgencyState> states;
    
    public ReconnectingMole(Mole otherMole) {
        this.remoteMole = otherMole;
        states = new OnErrorResubscriber<>(ImmutableAgencyState.of(ImmutableSet.of(), ImmutableSet.of()), otherMole::states).flux();
    }

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        return remoteMole.instantiate(mission, params);
    }

    @Override
    public Flux<AgencyState> states() {
        return states;
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return remoteMole.statesFor(handle);
    }

    @Override
    public Flux<MissionOutput> outputsFor(MissionHandle handle) {
        return remoteMole.outputsFor(handle);
    }

    @Override
    public Flux<MissionRepresentation> representationsFor(MissionHandle handle) {
        return remoteMole.representationsFor(handle);
    }

    @Override
    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return remoteMole.representationOf(mission);
    }

    @Override
    public Mono<MissionParameterDescription> parameterDescriptionOf(Mission mission) {
        return remoteMole.parameterDescriptionOf(mission);
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        remoteMole.instruct(handle, strand, command);
    }

    @Override
    public void instructRoot(MissionHandle handle, StrandCommand command) {
        remoteMole.instructRoot(handle, command);
        
    }

    @Override
    public void instructBlock(MissionHandle handle, String blockId, BlockCommand command) {
        remoteMole.instructBlock(handle, blockId, command);
        
    }

    @Override
    public void instruct(MissionHandle handle, MissionCommand command) {
        remoteMole.instruct(handle, command);
    }

}

