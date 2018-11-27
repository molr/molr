package org.molr.mole.remote.rest;

import org.molr.commons.domain.*;
import org.molr.commons.domain.dto.*;
import org.molr.mole.core.api.Mole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * this mole accesses a remote mole through a REST server
 */

public class RestRemoteMole implements Mole {

    private final static Logger LOGGER = LoggerFactory.getLogger(RestRemoteMole.class);

    private final WebClient client;

    public RestRemoteMole(String baseUrl) {
        requireNonNull(baseUrl, "baseUrl must not be null");
        client = WebClient.create(baseUrl);
    }

    @Override
    public Set<Mission> availableMissions() {
        String uri = "mission/availableMissions";
        return getResponseAsMono(uri, MissionSetDto.class).block().toMissionSet();
    }


    @Override
    public MissionRepresentation representationOf(Mission mission) {
        String uri = "mission/" + mission.name() + "/representation";
        Mono<MissionRepresentationDto> missionRepresentation = getResponseAsMono(uri, MissionRepresentationDto.class);
        return missionRepresentation.map(MissionRepresentationDto::toMissionRepresentation).block();
    }

    @Override
    public MissionParameterDescription parameterDescriptionOf(Mission mission) {
        Mono<MissionParameterDescriptionDto> missionRepresentation = getResponseAsMono("mission/" + mission.name() + "/parameterDescription", MissionParameterDescriptionDto.class);
        return missionRepresentation.map(MissionParameterDescriptionDto::toMissionParameterDescription).block();
    }

    @Override
    public void instantiate(MissionHandle handle, Mission mission, Map<String, Object> params) {
        String uri = "mission/" + mission.name() + "/instantiate/" + handle.id();
        Mono<ClientResponse> clientResponse = client.post().uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(params))
                .exchange();
        handleErrors(uri,clientResponse);
        clientResponse.doOnError(e -> LOGGER.error("Error during mission instantiation", e)).block();
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        Flux<MissionStateDto> response = getResponseAsFlux("mission/status/" + handle.id(), MissionStateDto.class);
        return response.map(MissionStateDto::toMissionState);
    }

    @Override
    public Flux<MissionOutput> outputsFor(MissionHandle handle) {
        Flux<MissionOutputDto> response = getResponseAsFlux("mission/outputsFor/" + handle.id(), MissionOutputDto.class);
        return response.map(MissionOutputDto::toMissionOutput);
    }

    @Override
    public Flux<MissionRepresentation> representationsFor(MissionHandle handle) {
        Flux<MissionRepresentationDto> response = getResponseAsFlux("mission/outputsFor/" + handle.id(), MissionRepresentationDto.class);
        return response.map(MissionRepresentationDto::toMissionRepresentation);
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        MissionHandleDto.from(handle);//{missionHandleId}/{strandId}/{standCommand}
        StrandDto.from(strand);
        String uri = "mission/instruct/" + handle.id() + "/" + strand.id() + "/" + command.name();
        Mono<ClientResponse> clientResponse = clientResponseForPost(uri, MediaType.APPLICATION_JSON);
        handleErrors(uri,clientResponse);
        clientResponse.doOnError(e -> LOGGER.error("Error during mission instantiation", e)).block();
    }

    private Mono<ClientResponse> clientResponseForGet(String uri, MediaType mediaType) {
        return client.get()
                .uri(uri)
                .accept(mediaType)
                .exchange();
    }

    private Mono<ClientResponse> clientResponseForPost(String uri, MediaType mediaType) {
        return client.post()
                .uri(uri)
                .accept(mediaType)
                .exchange();
    }

    private void handleErrors(String uri, Mono<ClientResponse> clientResponse) {
        HttpStatus responseStatus = clientResponse.block().statusCode();
        if (responseStatus == HttpStatus.NOT_FOUND) {
            throw new IllegalStateException("server response = NOT FOUND : may be a uri problem or wrong parameters ");
        } else if (responseStatus.isError()) {
            String errorMessage = clientResponse.block().bodyToMono(String.class).block();
            throw new IllegalArgumentException("error when calling " + uri
                    + " with http status " + responseStatus.name()
                    + " and error message " + errorMessage);
        }
    }


    private <T> Flux<T> getResponseAsFlux(String uri, Class<T> type) {
        Mono<ClientResponse> clientResponse = clientResponseForGet(uri, MediaType.APPLICATION_STREAM_JSON);
        handleErrors(uri, clientResponse);
        return clientResponse
                .doOnError(e -> LOGGER.error("Error while retrieving uri {}.", uri, e))
                .flatMapMany(response -> response.bodyToFlux(type));
    }

    private <T> Mono<T> getResponseAsMono(String uri, Class<T> type) {
        Mono<ClientResponse> clientResponse = clientResponseForGet(uri, MediaType.APPLICATION_JSON);
        handleErrors(uri, clientResponse);
        return clientResponse
                .doOnError(e -> LOGGER.error("Error while retrieving uri {}.", uri, e))
                .flatMap(response -> response.bodyToMono(type));
    }
}

