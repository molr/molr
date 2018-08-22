package org.molr.client.rest;

import org.molr.commons.api.domain.*;
import org.molr.commons.api.domain.dto.*;
import org.molr.commons.api.service.Agency;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class RestRemoteAgency implements Agency {

    private final WebClient client;

    public RestRemoteAgency(String baseUrl) {
        requireNonNull(baseUrl, "baseUrl must not be null");
        this.client = WebClient.create(baseUrl);
    }


    @Override
    public Flux<AgencyState> states() {
        return flux("/states", AgencyStateDto.class).map(AgencyStateDto::toAgencyState);
    }

    @Override
    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return mono("/mission/" + mission.name() + "/representation", MissionRepresentationDto.class)
                .map(MissionRepresentationDto::toMissionRepresentation);
    }

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        /* TODO treat parameters */
        Mono<MissionHandle> cache = mono("/mission/" + mission.name() + "/instantiate", MissionHandleDto.class)
                .map(MissionHandleDto::toMissionHandle).cache();
        /* This has to be a hot source, in order that the instantiation is executed, even if nobody is subscribed*/
        cache.subscribe();
        return cache;
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return flux("/instance/" + handle.id() + "/states", MissionStateDto.class)
                .map(MissionStateDto::toMissionState);
    }

    @Override
    public void instruct(MissionHandle handle, MissionCommand command) {
        /* TODO */
    }

    private <T> Flux<T> flux(String uri, Class<T> type) {
        return exchange(uri).flatMapMany(res -> res.bodyToFlux(type));
    }

    private <T> Mono<T> mono(String uri, Class<T> type) {
        return exchange(uri).flatMap(res -> {
            System.out.println("Response: " + res);
            return res.bodyToMono(type);
        });
    }

    private Mono<ClientResponse> exchange(String uri) {
        return client.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange();
    }

}
