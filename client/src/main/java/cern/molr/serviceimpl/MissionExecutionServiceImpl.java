/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.serviceimpl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import cern.molr.exception.UnsupportedOutputTypeException;
import cern.molr.mission.run.RunMissionController;
import cern.molr.mission.service.MissionExecutionService;
import cern.molr.mission.step.StepMissionController;
import cern.molr.type.Ack;
import cern.molr.rest.bean.MissionExecutionResponseBean;
import cern.molr.rest.request.MissionCancelRequest;
import cern.molr.rest.request.MissionExecutionRequest;
import cern.molr.rest.request.MissionResultRequest;
import cern.molr.rest.result.MissionCancelResponse;
import cern.molr.rest.result.MissionExecutionResponse;
import cern.molr.rest.result.MissionIntegerResponse;
import cern.molr.rest.result.MissionXResponse;
import reactor.core.publisher.Mono;

/**
 * Implementation used by the operator to interact with the server
 * 
 * @author nachivpn
 */
public class MissionExecutionServiceImpl implements MissionExecutionService{

    WebClient client = WebClient.create("http://localhost:8080");

    @Override
    public <I, O> CompletableFuture<RunMissionController<O>> runToCompletion(String missionDefnClassName, I args, Class<I> cI, Class<O> cO) {
        MissionExecutionRequest<I> execRequest = new MissionExecutionRequest<>(missionDefnClassName, args);
        return CompletableFuture.<RunMissionController<O>>supplyAsync(() -> {
            final String missionExecutionId = client.post().uri("/mission")
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromPublisher(Mono.just(execRequest), MissionExecutionRequest.class)).exchange()
                    .flatMapMany(value -> value.bodyToMono(MissionExecutionResponse.class))
                    .doOnError(throwable ->  {throw new CompletionException(throwable);})
                    .map(value -> value.match(
                            (Exception e) -> {throw new CompletionException(e);}, 
                            (MissionExecutionResponseBean resp) -> resp.getMissionExecutionId()))
                    .blockFirst();
            return new RunMissionController<O>() {
                @SuppressWarnings("unchecked")
                @Override
                public CompletableFuture<O> getResult() {
                    return CompletableFuture.supplyAsync(() ->{
                        MissionResultRequest resultRequest = new MissionResultRequest(missionExecutionId);
                        return (O) client.post().uri("/result")
                                .accept(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromPublisher(Mono.just(resultRequest), MissionResultRequest.class)).exchange()
                                .flatMapMany(value -> {
                                    try {
                                        return value.bodyToMono(getMissionResultResponseType(cO));
                                    } catch (UnsupportedOutputTypeException e) {
                                        throw new CompletionException("Cannot deserialize output", e);
                                    }
                                })
                                .doOnError(throwable ->  {throw new CompletionException("An error occured while getting result",throwable);})
                                .map(value -> value.match(
                                        (Exception e) -> {throw new CompletionException("Mission execution failed", e);}, 
                                        //Return the result as it is
                                        Function.identity()))
                                .blockFirst();
                    });
                }

                @Override
                public CompletableFuture<Ack> cancel() {
                    return CompletableFuture.supplyAsync(() ->{
                        MissionCancelRequest cancelRequest = new MissionCancelRequest(missionExecutionId);
                        return client.post().uri("/cancel")
                                .accept(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromPublisher(Mono.just(cancelRequest), MissionCancelRequest.class)).exchange()
                                .flatMapMany(value -> value.bodyToMono(MissionCancelResponse.class))
                                .doOnError(throwable ->  {throw new CompletionException("An error occured while cancelling",throwable);})
                                .map(value -> value.match(
                                        (Exception e) -> {throw new CompletionException("Cancel failed", e);}, 
                                        //Return the result as it is
                                        Function.identity()))
                                .blockFirst();
                    });
                }

            };
        });

    }

    @Override
    public <I, O> CompletableFuture<StepMissionController<O>> step(String missionDefnClassName, I args) {
        return null;
    }

    private Class<? extends MissionXResponse<?>> getMissionResultResponseType(Class<?> c) throws UnsupportedOutputTypeException {
        if(c.equals(Integer.class))
            return MissionIntegerResponse.class;
        else
            throw new UnsupportedOutputTypeException(c.getName() + "is not supported yet!");
    }


}
