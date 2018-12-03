package org.molr.agency.remote.rest;

import org.molr.agency.core.Agency;
import org.molr.commons.domain.*;
import org.molr.commons.domain.dto.*;
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

public class RestRemoteAgency implements Agency {

    private final static Logger LOGGER = LoggerFactory.getLogger(RestRemoteAgency.class);

    private final WebClientUtils clientUtils;

    public RestRemoteAgency(String baseUrl) {
        requireNonNull(baseUrl, "baseUrl must not be null");
        this.clientUtils = WebClientUtils.withBaseUrl(baseUrl);
    }


    /* Get requests */

    @Override
    public Flux<AgencyState> states() {
        return clientUtils.flux("/states", AgencyStateDto.class)
                .map(AgencyStateDto::toAgencyState);

    }

    @Override
    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return clientUtils.mono("/mission/" + mission.name() + "/representation", MissionRepresentationDto.class)
                .map(MissionRepresentationDto::toMissionRepresentation);
    }

    @Override
    public Mono<MissionParameterDescription> parameterDescriptionOf(Mission mission) {
        return clientUtils.mono("/mission/" + mission.name() + "/parameterDescription", MissionParameterDescriptionDto.class)
                .map(MissionParameterDescriptionDto::toMissionParameterDescription);
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return clientUtils.flux("/instance/" + handle.id() + "/states", MissionStateDto.class)
                .map(MissionStateDto::toMissionState);
    }

    @Override
    public Flux<MissionOutput> outputsFor(MissionHandle handle) {
        return clientUtils.flux("/instance/" + handle.id() + "/outputs", MissionOutputDto.class)
                .map(MissionOutputDto::toMissionOutput);
    }


    @Override
    public Flux<MissionRepresentation> representationsFor(MissionHandle handle) {
        return clientUtils.flux("/instance/" + handle.id() + "/representations", MissionRepresentationDto.class)
                .map(MissionRepresentationDto::toMissionRepresentation);
    }


    /* Post requests */

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        String uri = "/mission/" + mission.name() + "/instantiate";
        Mono<MissionHandleDto> responseBody = clientUtils
                .postMono(uri, APPLICATION_STREAM_JSON, fromObject(params), MissionHandleDto.class);
        return responseBody.map(MissionHandleDto::toMissionHandle);
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        clientUtils.post("/instance/" + handle.id() + "/" + strand.id() + "/instruct/" + command.name(), MediaType.APPLICATION_JSON, BodyInserters.empty());
    }

    @Override
    public void instructRoot(MissionHandle handle, StrandCommand command) {
        clientUtils.post("/instance/" + handle.id() + "/instructRoot/" + command.name(), MediaType.APPLICATION_JSON, BodyInserters.empty());
    }

}
