/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cern.molr.exception.UnknownMissionException;
import cern.molr.server.ServerMain;
import cern.molr.server.request.MissionCancelRequest;
import cern.molr.server.request.MissionExecutionRequest;
import cern.molr.server.request.MissionResultRequest;
import cern.molr.server.response.MissionCancelResponse;
import cern.molr.server.response.MissionCancelResponseFailure;
import cern.molr.server.response.MissionCancelResponseSuccess;
import cern.molr.server.response.MissionExecutionResponse;
import cern.molr.server.response.MissionExecutionResponseBean;
import cern.molr.server.response.MissionExecutionResponseFailure;
import cern.molr.server.response.MissionExecutionResponseSuccess;
import cern.molr.server.response.MissionGenericResponse;
import cern.molr.server.response.MissionGenericResponseFailure;
import cern.molr.server.response.MissionGenericResponseSuccess;
import cern.molr.server.service.ServerRestExecutionService;

/**
 * {@link RestController} for {@link ServerMain} spring application
 * 
 * @author nachivpn
 */
@RestController
public class ServerRestController {

    private final ServerRestExecutionService meGateway;

    @Autowired
    public ServerRestController(ServerRestExecutionService meGateway) {
        this.meGateway = meGateway;
    }

    @RequestMapping(path = "/mission", method = RequestMethod.POST)
    public <I> MissionExecutionResponse newMission(@RequestBody MissionExecutionRequest<I> request) {
        try {
            String mEId = meGateway.runMission(request.getMissionDefnClassName(), request.getArgs());
            return new MissionExecutionResponseSuccess(new MissionExecutionResponseBean(mEId));
        } catch (UnknownMissionException e) {
            /*
             * The idea behind not catching Exception is to ensure that any server related 
             * issues are not off-loaded to be handled by the client (same applies below)
             * Moreover, the Molr API does NOT define any runtime exceptions 
             * and catching any would be analogous to suppressing an issue!
             */
            return new MissionExecutionResponseFailure(e);
        }

    }

    @RequestMapping(path = "/result", method = RequestMethod.POST)
    public CompletableFuture<MissionGenericResponse<Object>> result(@RequestBody MissionResultRequest request) {
        try {
            return meGateway.getResult(request.getMissionExecutionId())
                    .<MissionGenericResponse<Object>>thenApply(MissionGenericResponseSuccess<Object>::new)
                    .exceptionally(MissionGenericResponseFailure<Object>::new);
        } catch (UnknownMissionException e) {
            return CompletableFuture.supplyAsync(() -> new MissionGenericResponseFailure<>(e));
        }
    }

    @RequestMapping(path = "/cancel", method = RequestMethod.POST)
    public CompletableFuture<MissionCancelResponse> result(@RequestBody MissionCancelRequest request) {
        try {
            return meGateway.cancel(request.getMissionExecutionId())
                    .<MissionCancelResponse>thenApply(MissionCancelResponseSuccess::new)
                    .exceptionally(MissionCancelResponseFailure::new);
        } catch (UnknownMissionException e) {
            return CompletableFuture.supplyAsync(() -> new MissionCancelResponseFailure(e));
        }
        
    }

}
