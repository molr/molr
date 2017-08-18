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
import cern.molr.commons.response.MissionCancelResponse;
import cern.molr.commons.response.MissionCancelResponse.MissionCancelResponseFailure;
import cern.molr.commons.response.MissionCancelResponse.MissionCancelResponseSuccess;
import cern.molr.commons.response.MissionGenericResponse;
import cern.molr.commons.response.MissionGenericResponse.MissionGenericResponseFailure;
import cern.molr.commons.response.MissionGenericResponse.MissionGenericResponseSuccess;
import cern.molr.mission.Mission;
import cern.molr.supervisor.request.MissionCancelRequest;
import cern.molr.supervisor.request.MissionExecutionRequest;

/**
 * {@link RestController} for {@link RemoteSupervisorMain} spring application
 * 
 * @author nachivpn
 */
@RestController
public class RemoteSupervisorController{

    private final RemoteSupervisorService supervisorService;

    public RemoteSupervisorController(RemoteSupervisorService service) {
        this.supervisorService = service;
    }

    @RequestMapping(path = "/run", method = RequestMethod.POST)
    public <I,O> Future<? extends MissionGenericResponse<O>> run(@RequestBody MissionExecutionRequest<I> request) {
        Mission m = new MissionImpl(request.getMoleClassName(), request.getMissionContentClassName());
        return supervisorService
        .<I,O>run(m, request.getArgs(), request.getMissionExecutionId())
        .<MissionGenericResponse<O>>thenApply(MissionGenericResponseSuccess<O>::new)
        .exceptionally(MissionGenericResponseFailure::new);
    }

    @RequestMapping(path = "/cancel", method = RequestMethod.POST)
    public Future<? extends MissionCancelResponse> cancel(@RequestBody  MissionCancelRequest request) {
        return supervisorService
        .cancel(request.getMissionExecutionId())
        .<MissionCancelResponse>thenApply(MissionCancelResponseSuccess::new)
        .exceptionally(MissionCancelResponseFailure::new);
    }

}
