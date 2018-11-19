package org.molr.mole.remote.rest;

import org.molr.commons.domain.*;
import org.molr.commons.domain.dto.*;
import org.molr.mole.core.api.Mole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class RestRemoteMole implements Mole {

    private final static Logger LOGGER = LoggerFactory.getLogger(RestRemoteMole.class);

    private final WebClient client;

    public RestRemoteMole(String baseUrl) {
        requireNonNull(baseUrl, "baseUrl must not be null");
        client = WebClient.create(baseUrl);
    }

    @Override
    public Set<Mission> availableMissions() {
        //probably a better way to do it...
        Set<Mission> missionSet = new HashSet<>();
        Flux<MissionDto> missions = getResponseAsFlux("mission/availableMission",MissionDto.class);
        missions.map(MissionDto::toMission).map(m->missionSet.add(m));
        return missionSet;
    }

    @Override
    public MissionRepresentation representationOf(Mission mission) {
        Mono<MissionRepresentationDto> missionRepresentation = getResponseAsMono("mission/" +mission.name() + "/representation", MissionRepresentationDto.class);
        return missionRepresentation.map(MissionRepresentationDto::toMissionRepresentation).block();
    }

    @Override
    public MissionParameterDescription parameterDescriptionOf(Mission mission) {
        Mono<MissionParameterDescriptionDto> missionRepresentation = getResponseAsMono("mission/" +mission.name() + "/parameterDescription", MissionParameterDescriptionDto.class);
        return missionRepresentation.map(MissionParameterDescriptionDto::toMissionParameterDescription).block();
    }

    @Override
    public void instantiate(MissionHandle handle, Mission mission, Map<String, Object> params) {
        client.post().uri("mission/"+mission.name()+"/instanciate/"+handle.id())
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(params))
                .exchange()
                .doOnError(e->LOGGER.error("Error during mission instantiation",e))
                .subscribe();

    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        Flux<MissionStateDto> response =  getResponseAsFlux("mission/status/" + handle.id(), MissionStateDto.class);
        return response.map(MissionStateDto::toMissionState);
    }

    @Override
    public Flux<MissionOutput> outputsFor(MissionHandle handle) {
        Flux<MissionOutputDto> response =  getResponseAsFlux("mission/outputsFor/" + handle.id(), MissionOutputDto.class);
        return response.map(MissionOutputDto::toMissionOutput);
    }

    @Override
    public Flux<MissionRepresentation> representationsFor(MissionHandle handle) {
        Flux<MissionRepresentationDto> response =  getResponseAsFlux("mission/outputsFor/" + handle.id(), MissionRepresentationDto.class);
        return response.map(MissionRepresentationDto::toMissionRepresentation);
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        MissionHandleDto.from(handle);//{missionHandleId}/{strandId}/{standCommand}
        StrandDto.from(strand);
        client.post().uri("mission/instruct/"+handle.id()+"/"+strand.id()+"/"+command.name())
                .exchange()
                .doOnError(e -> LOGGER.error("error while instructing command {} to strand {} on handle {}.", command, strand, handle, e))
                .subscribe();
    }

    private <T> Flux<T> getResponseAsFlux(String uri, Class<T> type){
       return client.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .flatMapMany(response -> response.bodyToFlux(type));
    }

    private <T> Mono<T> getResponseAsMono(String uri, Class<T> type){
        return client.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .flatMap(response -> response.bodyToMono(type));
    }
}

