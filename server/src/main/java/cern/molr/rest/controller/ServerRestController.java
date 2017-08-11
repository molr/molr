/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.rest.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cern.molr.exception.MissionExecutionException;
import cern.molr.rest.bean.MissionExecutionResponseBean;
import cern.molr.rest.request.MissionCancelRequest;
import cern.molr.rest.request.MissionExecutionRequest;
import cern.molr.rest.request.MissionResultRequest;
import cern.molr.rest.result.MissionCancelResponse;
import cern.molr.rest.result.MissionCancelResponseFailure;
import cern.molr.rest.result.MissionCancelResponseSuccess;
import cern.molr.rest.result.MissionExecutionResponse;
import cern.molr.rest.result.MissionExecutionResponseFailure;
import cern.molr.rest.result.MissionExecutionResponseSuccess;
import cern.molr.rest.result.MissionXResponse;
import cern.molr.rest.result.MissionXResponseFailure;
import cern.molr.rest.result.MissionXResponseSuccess;
import cern.molr.rest.service.ServerRestExecutionService;
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
