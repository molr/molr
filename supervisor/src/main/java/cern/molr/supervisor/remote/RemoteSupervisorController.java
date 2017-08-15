/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.remote;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cern.molr.commons.MissionImpl;
import cern.molr.mission.Mission;
import cern.molr.mission.step.StepResult;
import cern.molr.supervisor.request.MissionExecutionRequest;
import cern.molr.supervisor.response.MissionExecutionResponse;
import cern.molr.supervisor.response.MissionExecutionResponseFailure;
import cern.molr.supervisor.response.MissionExecutionResponseSuccess;
import cern.molr.type.Ack;
import cern.molr.type.either.Either;

@RestController
public class RemoteSupervisorController{

    private final RemoteSupervisorService supervisorService;

    public RemoteSupervisorController(RemoteSupervisorService service) {
        this.supervisorService = service;
    }

    @RequestMapping(path = "/run", method = RequestMethod.POST)
    public <I,O> MissionExecutionResponse run(@RequestBody MissionExecutionRequest<I> request) {
        Mission m = new MissionImpl(request.getMoleClassName(), request.getMissionContentClassName());
        try {
            @SuppressWarnings("unchecked")
            O result = (O) supervisorService.run(m, request.getArgs(), request.getMissionExecutionId()).get();
            return new MissionExecutionResponseSuccess(result);
        } catch (Exception e) {
            return new MissionExecutionResponseFailure(e);
        }
    }

    @RequestMapping(path = "/step", method = RequestMethod.POST)
    public <I, O> CompletableFuture<Either<StepResult, O>> step(Mission m, I args, String missionExecutionId) {
        //TODO
        return supervisorService.step(m, args, missionExecutionId);
    }

    @RequestMapping(path = "/resume", method = RequestMethod.POST)
    public <I, O> CompletableFuture<O> resume(Mission m, I args, String missionExecutionId) {
        //TODO
        return supervisorService.resume(m, args, missionExecutionId);
    }

    @RequestMapping(path = "/cancel", method = RequestMethod.POST)
    public CompletableFuture<Ack> cancel() {
        //TODO
        return supervisorService.cancel();
    }




}
