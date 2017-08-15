/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import cern.molr.mission.Mission;
import cern.molr.mission.step.StepResult;
import cern.molr.mole.supervisor.MoleSupervisor;
import cern.molr.supervisor.request.MissionExecutionRequest;
import cern.molr.supervisor.response.MissionExecutionResponse;
import cern.molr.type.Ack;
import cern.molr.type.either.Either;
import cern.molr.web.MolrWebClient;

public class RemoteSupervisor implements MoleSupervisor{

    private MolrWebClient client;

    public RemoteSupervisor(String host, int port) {
        this.client = new MolrWebClient(host, port);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> CompletableFuture<O> run(Mission m, I args, String missionExecutionId) {
        MissionExecutionRequest<I> execRequest = 
                new MissionExecutionRequest<>(missionExecutionId, m.getMoleClassName(), m.getMissionDefnClassName(), args);
        return client.post("/run", MissionExecutionRequest.class, execRequest, MissionExecutionResponse.class)
                .thenApply(tryResp -> tryResp.match(
                        (Throwable e) -> {throw new CompletionException(e);}, 
                        resp -> (O) resp
                        ));
    }

    @Override
    public <I, O> CompletableFuture<Either<StepResult, O>> step(Mission m, I args, String missionExecutionId) {
        throw new RuntimeException("step has not been implemented!");
    }

    @Override
    public <I, O> CompletableFuture<O> resume(Mission m, I args, String missionExecutionId) {
        throw new RuntimeException("resume has not been implemented!");
    }

    @Override
    public CompletableFuture<Ack> cancel(String missionExecutionId) {
        throw new RuntimeException("cancel has not been implemented!");
    }

}
