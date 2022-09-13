package io.molr.mole.remote.rest;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON;//replace with _NDJSON
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import io.molr.commons.domain.AgencyState;
import io.molr.commons.domain.BlockCommand;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionCommand;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionOutput;
import io.molr.commons.domain.MissionParameterDescription;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.MissionState;
import io.molr.commons.domain.Strand;
import io.molr.commons.domain.StrandCommand;
import io.molr.commons.domain.dto.AgencyStateDto;
import io.molr.commons.domain.dto.MissionHandleDto;
import io.molr.commons.domain.dto.MissionOutputDto;
import io.molr.commons.domain.dto.MissionParameterDescriptionDto;
import io.molr.commons.domain.dto.MissionRepresentationDto;
import io.molr.commons.domain.dto.MissionStateDto;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.api.MoleWebApi;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RestRemoteMole implements Mole {

    private final MoleWebClient clientUtils;

    public RestRemoteMole(String baseUrl) {
        requireNonNull(baseUrl, "baseUrl must not be null");
        this.clientUtils = MoleWebClient.withBaseUrl(baseUrl);
    }


    /* Get requests */

    @Override
    public Flux<AgencyState> states() {
        return clientUtils.flux(MoleWebApi.AGENCY_STATES, AgencyStateDto.class)
                .map(AgencyStateDto::toAgencyState);

    }

    @Override
    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return clientUtils.mono(MoleWebApi.missionRepresentationUrl(mission.name()), MissionRepresentationDto.class)
                .map(MissionRepresentationDto::toMissionRepresentation);
    }

    @Override
    public Mono<MissionParameterDescription> parameterDescriptionOf(Mission mission) {
        return clientUtils.mono(MoleWebApi.missionParameterDescriptionUrl(mission.name()), MissionParameterDescriptionDto.class)
                .map(MissionParameterDescriptionDto::toMissionParameterDescription);
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return clientUtils.flux(MoleWebApi.instanceStatesUrl(handle.id()), MissionStateDto.class)
                .map(MissionStateDto::toMissionState);
    }

    @Override
    public Flux<MissionOutput> outputsFor(MissionHandle handle) {
        return clientUtils.flux(MoleWebApi.instanceOutputsUrl(handle.id()), MissionOutputDto.class)
                .map(MissionOutputDto::toMissionOutput);
    }

    @Override
    public Flux<MissionRepresentation> representationsFor(MissionHandle handle) {
        return clientUtils.flux(MoleWebApi.instanceRepresentationsUrl(handle.id()), MissionRepresentationDto.class)
                .map(MissionRepresentationDto::toMissionRepresentation);
    }

    @Override
    public Mono<Map<String, Object>> inputFor(MissionHandle handle) {
        return clientUtils.mono(MoleWebApi.instanceInputUrl(handle.id()), 
                new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /* Post requests */

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        String uri = MoleWebApi.instantiateMission(mission.name());
        Mono<MissionHandleDto> responseBody = clientUtils
                .postMono(uri, APPLICATION_STREAM_JSON, fromValue(params), MissionHandleDto.class);
        return responseBody.map(MissionHandleDto::toMissionHandle);
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        clientUtils.post(MoleWebApi.instructInstance(handle.id(),strand.id(),command.name()), MediaType.APPLICATION_JSON, BodyInserters.empty());
    }

    @Override
    public void instructRoot(MissionHandle handle, StrandCommand command) {
        clientUtils.post(MoleWebApi.instructRootInstance(handle.id(),command.name()), MediaType.APPLICATION_JSON, BodyInserters.empty());
    }


    @Override
    public void instructBlock(MissionHandle handle, String blockId, BlockCommand command) {
        clientUtils.post(MoleWebApi.instructBlockInstance(handle.id(), blockId, command.name()), MediaType.APPLICATION_JSON, BodyInserters.empty());        
    }
    
    @Override
    public void instruct(final MissionHandle handle, final MissionCommand command) {
        clientUtils.post(MoleWebApi.instructMission(handle.id(), command.name()), MediaType.APPLICATION_JSON,
                BodyInserters.empty());
    }

}
