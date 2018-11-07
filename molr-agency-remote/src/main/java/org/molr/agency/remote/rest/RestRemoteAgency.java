package org.molr.agency.remote.rest;

import org.molr.commons.domain.*;
import org.molr.commons.domain.dto.*;
import org.molr.agency.core.Agency;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

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
    public Mono<MissionParameterDescription> parameterDescriptionOf(Mission mission) {
        return mono("/mission/" + mission.name() + "/parameter-description", MissionParameterDescriptionDto.class)
                .map(MissionParameterDescriptionDto::toMissionParameterDescription);
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
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        client.get()
                .uri("/instance/" + handle.id() + "/" + strand.id() + "/instruct/" + command.name())
                .exchange().subscribe();
    }

    private <T> Flux<T> flux(String uri, Class<T> type) {
        return exchange(uri).flatMapMany(res -> res.bodyToFlux(type));
    }

    private <T> Mono<T> mono(String uri, Class<T> type) {
        return exchange(uri).flatMap(res -> {
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
