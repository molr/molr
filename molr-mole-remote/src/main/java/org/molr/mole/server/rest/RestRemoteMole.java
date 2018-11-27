package org.molr.mole.server.rest;

import org.molr.commons.domain.*;
import org.molr.commons.domain.dto.*;
import org.molr.mole.core.api.Mole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
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
        return mono("mission/availableMissions", MissionSetDto.class)
                .map(MissionSetDto::toMissionSet)
                .block();
    }

    @Override
    public MissionRepresentation representationOf(Mission mission) {
        return mono("mission/" + mission.name() + "/representation", MissionRepresentationDto.class)
                .map(MissionRepresentationDto::toMissionRepresentation)
                .block();
    }

    @Override
    public MissionParameterDescription parameterDescriptionOf(Mission mission) {
        return mono("mission/" + mission.name() + "/parameterDescription", MissionParameterDescriptionDto.class)
                .map(MissionParameterDescriptionDto::toMissionParameterDescription)
                .block();
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return flux("instance/" + handle.id() + "/states", MissionStateDto.class)
                .map(MissionStateDto::toMissionState);
    }

    @Override
    public Flux<MissionOutput> outputsFor(MissionHandle handle) {
        return flux("instance/" + handle.id() + "/outputs", MissionOutputDto.class)
                .map(MissionOutputDto::toMissionOutput);
    }


    /* to fix*/
    @Override
    public Flux<MissionRepresentation> representationsFor(MissionHandle handle) {
        return flux("instance/" + handle.id() + "/representations", MissionRepresentationDto.class)
                .map(MissionRepresentationDto::toMissionRepresentation);
    }

    @Override
    public void instantiate(MissionHandle handle, Mission mission, Map<String, Object> params) {
        String uri = "mission/" + mission.name() + "/instantiate/" + handle.id();
        post(uri, MediaType.APPLICATION_JSON, BodyInserters.fromObject(params));
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        String uri = "instance/" + handle.id() + "/" + strand.id() + "/instruct/" + command.name();
        post(uri, MediaType.APPLICATION_JSON, BodyInserters.empty());
    }


    private static final void throwOnErrors(String uri, Mono<ClientResponse> clientResponse) {
        HttpStatus responseStatus = clientResponse.block().statusCode();
        if (responseStatus == HttpStatus.NOT_FOUND) {
            throw new IllegalStateException("Server response = NOT FOUND : may be a uri problem or wrong parameters. Uri was: '" + uri + "'.");
        } else if (responseStatus.isError()) {
            String errorMessage = clientResponse.block().bodyToMono(String.class).block();
            throw new IllegalArgumentException("error when calling " + uri
                    + " with http status " + responseStatus.name()
                    + " and error message: \n" + errorMessage);
        }
    }

    private <T> Flux<T> flux(String uri, Class<T> type) {
        Mono<ClientResponse> clientResponse = clientResponseForGet(uri, MediaType.APPLICATION_STREAM_JSON);
        throwOnErrors(uri, clientResponse);
        return clientResponse
                .doOnError(e -> LOGGER.error("Error while retrieving uri {}.", uri, e))
                .flatMapMany(response -> response.bodyToFlux(type));
    }

    private <T> Mono<T> mono(String uri, Class<T> type) {
        Mono<ClientResponse> clientResponse = clientResponseForGet(uri, MediaType.APPLICATION_JSON);
        throwOnErrors(uri, clientResponse);
        return clientResponse
                .doOnError(e -> LOGGER.error("Error while retrieving uri {}.", uri, e))
                .flatMap(response -> response.bodyToMono(type));
    }

    private Mono<ClientResponse> clientResponseForGet(String uri, MediaType mediaType) {
        return client.get()
                .uri(uri)
                .accept(mediaType)
                .exchange();
    }

    private void post(String uri, MediaType mediaType, BodyInserter<?, ? super ClientHttpRequest> body) {
        Mono<ClientResponse> response = client.post()
                .uri(uri)
                .accept(mediaType)
                .body(body)
                .exchange();
        throwOnErrors(uri, response);
        response.doOnError(e -> LOGGER.error("Posting request on URI {}.", uri, e)).subscribe();
    }

}

