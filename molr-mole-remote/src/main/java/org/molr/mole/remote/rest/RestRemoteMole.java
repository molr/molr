package org.molr.mole.remote.rest;

import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.MissionOutput;
import org.molr.commons.domain.MissionParameterDescription;
import org.molr.commons.domain.MissionRepresentation;
import org.molr.commons.domain.MissionState;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;
import org.molr.commons.domain.dto.MissionOutputDto;
import org.molr.commons.domain.dto.MissionParameterDescriptionDto;
import org.molr.commons.domain.dto.MissionRepresentationDto;
import org.molr.commons.domain.dto.MissionSetDto;
import org.molr.commons.domain.dto.MissionStateDto;
import org.molr.commons.util.Strands;
import org.molr.mole.core.api.Mole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * this mole accesses a remote mole through a REST server
 */

public class RestRemoteMole implements Mole {

    private final static Logger LOGGER = LoggerFactory.getLogger(RestRemoteMole.class);

    private final WebClientUtils clientUtils;

    public RestRemoteMole(String baseUrl) {
        requireNonNull(baseUrl, "baseUrl must not be null");
        clientUtils = WebClientUtils.withBaseUrl(baseUrl);
    }

    @Override
    public Set<Mission> availableMissions() {
        return clientUtils.mono("mission/availableMissions", MissionSetDto.class)
                .map(MissionSetDto::toMissionSet)
                .block();
    }

    @Override
    public MissionRepresentation representationOf(Mission mission) {
        return clientUtils.mono("mission/" + mission.name() + "/representation", MissionRepresentationDto.class)
                .map(MissionRepresentationDto::toMissionRepresentation)
                .block();
    }

    @Override
    public MissionParameterDescription parameterDescriptionOf(Mission mission) {
        return clientUtils.mono("mission/" + mission.name() + "/parameterDescription", MissionParameterDescriptionDto.class)
                .map(MissionParameterDescriptionDto::toMissionParameterDescription)
                .block();
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
    public void instantiate(MissionHandle handle, Mission mission, Map<String, Object> params) {
        String uri = "mission/" + mission.name() + "/instantiate/" + handle.id();
        clientUtils.post(uri, MediaType.APPLICATION_JSON, BodyInserters.fromObject(params));
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

}

