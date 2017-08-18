/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

import cern.molr.commons.web.MolrWebClient;
import cern.molr.mission.Mission;
import cern.molr.mission.step.StepResult;
import cern.molr.mole.supervisor.MoleSupervisor;
import cern.molr.server.response.MissionCancelResponse;
import cern.molr.server.response.MissionObjectResponse;
import cern.molr.supervisor.request.MissionCancelRequest;
import cern.molr.supervisor.request.MissionExecutionRequest;
import cern.molr.type.Ack;
import cern.molr.type.either.Either;

/**
 * {@link RemoteSupervisor} is the remote implementation of the {@link MoleSupervisor}
 * 
 * Ideally, this object would be constructed by the infrastructure delegate which ensures that 
 * a mole supervisor is up and running on a specific host at a specific port
 * 
 * @author nachivpn
 */
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
        return client.post("/run", MissionExecutionRequest.class, execRequest, MissionObjectResponse.class)
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
        MissionCancelRequest cancelRequest = new MissionCancelRequest(missionExecutionId);
        return client.post("/cancel", MissionCancelRequest.class, cancelRequest, MissionCancelResponse.class)
                .thenApply(tryResp -> tryResp.match(
                        (Throwable e) -> {throw new CompletionException(e);}, 
                        Function.identity()
                        ));
    }

}
