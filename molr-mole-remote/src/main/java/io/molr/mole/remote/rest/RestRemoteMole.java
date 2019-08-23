package io.molr.mole.remote.rest;

import io.molr.commons.domain.*;
import io.molr.commons.domain.dto.*;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.api.MoleWebApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

public class RestRemoteMole implements Mole {

    private final static Logger LOGGER = LoggerFactory.getLogger(RestRemoteMole.class);

    private final MoleWebClient clientUtils;

    public RestRemoteMole(String baseUrl) {
        requireNonNull(baseUrl, "baseUrl must not be null");
        this.clientUtils = MoleWebClient.withBaseUrl(baseUrl);
    }


    /* Get requests */

    @Override
    public Flux<AgencyState> states() {
        return clientUtils.flux("/states", AgencyStateDto.class)
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
    public Flux<MissionLog> logsFor(MissionHandle handle) {
        return clientUtils.flux(MoleWebApi.instanceLogsUrl(handle.id()), MissionLogDto.class)
                .map(MissionLogDto::toMissionLog);
    }

    /* Post requests */

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        String uri = MoleWebApi.instantiateMission(mission.name());
        Mono<MissionHandleDto> responseBody = clientUtils
                .postMono(uri, APPLICATION_STREAM_JSON, fromObject(params), MissionHandleDto.class);
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

}
