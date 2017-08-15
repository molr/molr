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

import cern.molr.exception.MissionExecutionException;
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
import cern.molr.server.response.MissionXResponse;
import cern.molr.server.response.MissionXResponseFailure;
import cern.molr.server.response.MissionXResponseSuccess;
import cern.molr.server.service.ServerRestExecutionService;
import cern.molr.type.Ack;

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
        } catch (Exception e) {
            return new MissionExecutionResponseFailure(e);
        }
        
    }

    @RequestMapping(path = "/result", method = RequestMethod.POST)
    public CompletableFuture<MissionXResponse<Object>> result(@RequestBody MissionResultRequest request) {
        return CompletableFuture.supplyAsync(() ->{
            try {
                CompletableFuture<?> resultFuture = meGateway.getResult(request.getMissionExecutionId());
                return new MissionXResponseSuccess<>(resultFuture.get());
            } catch (Exception e) {
                return new MissionXResponseFailure<>(new MissionExecutionException(e));
            }
        });
    }
    
    @RequestMapping(path = "/cancel", method = RequestMethod.POST)
    public CompletableFuture<MissionCancelResponse> result(@RequestBody MissionCancelRequest request) {
        return CompletableFuture.supplyAsync(() ->{
            try {
                CompletableFuture<Ack> resultFuture = meGateway.cancel(request.getMissionExecutionId());
                return new MissionCancelResponseSuccess(resultFuture.get());
            } catch (Exception e) {
                return new MissionCancelResponseFailure(e);
            }
        });
    }

}
