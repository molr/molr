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
import cern.molr.server.response.MissionCancelResponse;
import cern.molr.server.response.MissionCancelResponseFailure;
import cern.molr.server.response.MissionCancelResponseSuccess;
import cern.molr.server.response.MissionXResponse;
import cern.molr.server.response.MissionXResponseFailure;
import cern.molr.server.response.MissionXResponseSuccess;
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
    public <I,O> Future<? extends MissionXResponse<O>> run(@RequestBody MissionExecutionRequest<I> request) {
        Mission m = new MissionImpl(request.getMoleClassName(), request.getMissionContentClassName());
        return supervisorService
        .<I,O>run(m, request.getArgs(), request.getMissionExecutionId())
        .<MissionXResponse<O>>thenApply(MissionXResponseSuccess<O>::new)
        .exceptionally(MissionXResponseFailure::new);
    }

    @RequestMapping(path = "/cancel", method = RequestMethod.POST)
    public Future<? extends MissionCancelResponse> cancel(@RequestBody  MissionCancelRequest request) {
        return supervisorService
        .cancel(request.getMissionExecutionId())
        .<MissionCancelResponse>thenApply(MissionCancelResponseSuccess::new)
        .exceptionally(MissionCancelResponseFailure::new);
    }

}
