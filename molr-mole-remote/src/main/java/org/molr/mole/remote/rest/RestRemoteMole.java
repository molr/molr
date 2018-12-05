package org.molr.mole.remote.rest;

import org.molr.commons.api.Agent;
import org.molr.commons.domain.*;
import org.molr.commons.domain.dto.*;
import org.molr.commons.util.Strands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * this mole accesses a remote mole through a REST server
 */

public class RestRemoteMole implements Agent {

    private final static Logger LOGGER = LoggerFactory.getLogger(RestRemoteMole.class);

    private final WebClientUtils clientUtils;

    public RestRemoteMole(String baseUrl) {
        requireNonNull(baseUrl, "baseUrl must not be null");
        clientUtils = WebClientUtils.withBaseUrl(baseUrl);
    }

    @Override
    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return clientUtils.mono("mission/" + mission.name() + "/representation", MissionRepresentationDto.class)
                .map(MissionRepresentationDto::toMissionRepresentation);
    }

    @Override
    public Mono<MissionParameterDescription> parameterDescriptionOf(Mission mission) {
        return clientUtils.mono("mission/" + mission.name() + "/parameterDescription", MissionParameterDescriptionDto.class)
                .map(MissionParameterDescriptionDto::toMissionParameterDescription);
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return clientUtils.flux("instance/" + handle.id() + "/states", MissionStateDto.class)
                .map(MissionStateDto::toMissionState);
    }

    @Override
    public Flux<MissionOutput> outputsFor(MissionHandle handle) {
        return clientUtils.flux("instance/" + handle.id() + "/outputs", MissionOutputDto.class)
                .map(MissionOutputDto::toMissionOutput);
    }


    /* to fix*/
    @Override
    public Flux<MissionRepresentation> representationsFor(MissionHandle handle) {
        return clientUtils.flux("instance/" + handle.id() + "/representations", MissionRepresentationDto.class)
                .map(MissionRepresentationDto::toMissionRepresentation);
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        String uri = "instance/" + handle.id() + "/" + strand.id() + "/instruct/" + command.name();
        clientUtils.post(uri, MediaType.APPLICATION_JSON, BodyInserters.empty());
    }

    @Override
    public void instructRoot(MissionHandle handle, StrandCommand command) {
        String uri = "instance/" + handle.id() + "/" + Strands.rootStrandPlaceholder() + "/instruct/" + command.name();
        clientUtils.post(uri, MediaType.APPLICATION_JSON, BodyInserters.empty());
    }

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        String uri = "mission/" + mission.name() + "/instantiate";
        return clientUtils.postMono(uri, MediaType.APPLICATION_JSON, BodyInserters.fromObject(params), MissionHandleDto.class)
                .map(MissionHandleDto::toMissionHandle);
    }

    @Override
    public Flux<AgencyState> states() {
        return clientUtils.flux("/states", AgencyStateDto.class)
                .map(AgencyStateDto::toAgencyState);
    }
}
