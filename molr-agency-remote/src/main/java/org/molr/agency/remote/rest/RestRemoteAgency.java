package org.molr.agency.remote.rest;

import org.molr.commons.domain.*;
import org.molr.commons.domain.dto.*;
import org.molr.agency.core.Agency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

public class RestRemoteAgency implements Agency {

    private final static Logger LOGGER = LoggerFactory.getLogger(RestRemoteAgency.class);

    private final WebClient client;

    public RestRemoteAgency(String baseUrl) {
        requireNonNull(baseUrl, "baseUrl must not be null");
        this.client = WebClient.create(baseUrl);
    }


    /* Get requests */

    @Override
    public Flux<AgencyState> states() {
        return flux("/states", AgencyStateDto.class)
                .map(AgencyStateDto::toAgencyState)
                .doOnError(e -> LOGGER.error("error while retrieving agency states", e));

    }

    @Override
    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return mono("/mission/" + mission.name() + "/representation", MissionRepresentationDto.class)
                .map(MissionRepresentationDto::toMissionRepresentation)
                .doOnError(e -> LOGGER.error("error while retrieving representation for mission'" + mission + "'", e));
    }

    @Override
    public Mono<MissionParameterDescription> parameterDescriptionOf(Mission mission) {
        return mono("/mission/" + mission.name() + "/parameter-description", MissionParameterDescriptionDto.class)
                .map(MissionParameterDescriptionDto::toMissionParameterDescription)
                .doOnError(e -> LOGGER.error("error while retrieving parameter description", e));
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return flux("/instance/" + handle.id() + "/states", MissionStateDto.class)
                .map(MissionStateDto::toMissionState)
                .doOnError(e -> LOGGER.error("error while retrieving mission states for handle '" + handle + "'", e));
    }

    @Override
    public Flux<MissionOutput> outputsFor(MissionHandle handle) {
        return flux("/instance/" + handle.id() + "/outputs", MissionOutputDto.class)
                .map(MissionOutputDto::toMissionOutput)
                .doOnError(e -> LOGGER.error("error while retrieving mission outputs for handle '" + handle + "'", e));
    }


    /* Post requests */

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        /* TODO treat parameters */
        Mono<MissionHandle> cache = client.post()
                .uri("/mission/" + mission.name() + "/instantiate")
                .body(fromObject(params))
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange()
                .flatMap(res -> res.bodyToMono(MissionHandleDto.class))
                .map(MissionHandleDto::toMissionHandle)
                .cache()
                .doOnError(e -> LOGGER.error("error while instantiating mission", e));
        /* This has to be a hot source, in order that the instantiation is executed, even if nobody is subscribed*/
        cache.subscribe();
        return cache;
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        client.post()
                .uri("/instance/" + handle.id() + "/" + strand.id() + "/instruct/" + command.name())
                .exchange()
                .doOnError(e -> LOGGER.error("error while instructing command {} to strand {} on handle {}.", command, strand, handle, e))
                .subscribe();
    }

    private <T> Flux<T> flux(String uri, Class<T> type) {
        return exchange(uri).flatMapMany(res -> res.bodyToFlux(type));
    }

    private <T> Mono<T> mono(String uri, Class<T> type) {
        return exchange(uri).flatMap(res -> res.bodyToMono(type));
    }

    private Mono<ClientResponse> exchange(String uri) {
        return client.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange();
    }

}
