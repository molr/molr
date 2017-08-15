/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import cern.molr.mission.Mission;
import cern.molr.mission.step.StepResult;
import cern.molr.mole.supervisor.MoleSupervisor;
import cern.molr.supervisor.request.MissionExecutionRequest;
import cern.molr.supervisor.response.MissionExecutionResponse;
import cern.molr.type.Ack;
import cern.molr.type.either.Either;
import reactor.core.publisher.Mono;

public class RemoteSupervisor implements MoleSupervisor{

    private WebClient client;

    public RemoteSupervisor(String host, int port) {
        this.client = WebClient.create("http://"+host+":"+port);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> CompletableFuture<O> run(Mission m, I args, String missionExecutionId) {
        MissionExecutionRequest<I> execRequest = 
                new MissionExecutionRequest<>(missionExecutionId, m.getMoleClassName(), m.getMissionDefnClassName(), args);
        return CompletableFuture.supplyAsync(() ->{
            return (O) client.post().uri("/run")
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromPublisher(Mono.just(execRequest), MissionExecutionRequest.class)).exchange()
                    .flatMapMany(value -> value.bodyToMono(MissionExecutionResponse.class))
                    .doOnError(throwable ->  {throw new CompletionException(throwable);})
                    .map(value -> value.match(
                            (Exception e) -> {throw new CompletionException(e);}, 
                            (Object resp) -> resp))
                    .blockFirst();
        });
    }

    @Override
    public <I, O> CompletableFuture<Either<StepResult, O>> step(Mission m, I args, String missionExecutionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <I, O> CompletableFuture<O> resume(Mission m, I args, String missionExecutionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompletableFuture<Ack> cancel() {
        // TODO Auto-generated method stub
        return null;
    }

}
