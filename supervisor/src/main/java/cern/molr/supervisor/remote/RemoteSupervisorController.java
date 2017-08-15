/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.remote;

import java.util.concurrent.Future;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cern.molr.commons.MissionImpl;
import cern.molr.mission.Mission;
import cern.molr.supervisor.request.MissionCancelRequest;
import cern.molr.supervisor.request.MissionExecutionRequest;
import cern.molr.supervisor.response.MissionCancelResponse;
import cern.molr.supervisor.response.MissionCancelResponseFailure;
import cern.molr.supervisor.response.MissionCancelResponseSuccess;
import cern.molr.supervisor.response.MissionExecutionResponse;
import cern.molr.supervisor.response.MissionExecutionResponseFailure;
import cern.molr.supervisor.response.MissionExecutionResponseSuccess;

@RestController
public class RemoteSupervisorController{

    private final RemoteSupervisorService supervisorService;

    public RemoteSupervisorController(RemoteSupervisorService service) {
        this.supervisorService = service;
    }

    @RequestMapping(path = "/run", method = RequestMethod.POST)
    public <I,O> Future<? extends MissionExecutionResponse> run(@RequestBody MissionExecutionRequest<I> request) {
        Mission m = new MissionImpl(request.getMoleClassName(), request.getMissionContentClassName());
        return supervisorService
        .<I,O>run(m, request.getArgs(), request.getMissionExecutionId())
        .<MissionExecutionResponse>thenApply(result -> new MissionExecutionResponseSuccess(result))
        .exceptionally(e -> new MissionExecutionResponseFailure(e));
    }

    @RequestMapping(path = "/cancel", method = RequestMethod.POST)
    public Future<? extends MissionCancelResponse> cancel(@RequestBody  MissionCancelRequest request) {
        return supervisorService
        .cancel(request.getMissionExecutionId())
        .<MissionCancelResponse>thenApply(ack -> new MissionCancelResponseSuccess(ack))
        .exceptionally(e -> new MissionCancelResponseFailure(e));
    }

    //    @RequestMapping(path = "/step", method = RequestMethod.POST)
    //    public <I, O> CompletableFuture<Either<StepResult, O>> step(Mission m, I args, String missionExecutionId) {
    //        return supervisorService.step(m, args, missionExecutionId);
    //    }
    //
    //    @RequestMapping(path = "/resume", method = RequestMethod.POST)
    //    public <I, O> CompletableFuture<O> resume(Mission m, I args, String missionExecutionId) {
    //        return supervisorService.resume(m, args, missionExecutionId);
    //    }

}
