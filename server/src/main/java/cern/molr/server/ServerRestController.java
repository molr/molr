/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server;

import java.util.concurrent.CompletableFuture;

import cern.molr.commons.response.*;
import cern.molr.exception.NoAppropriateSupervisorFound;
import cern.molr.commons.request.supervisor.SupervisorRegisterRequest;
import cern.molr.commons.request.supervisor.SupervisorUnregisterRequest;
import cern.molr.server.supervisor.StatefulMoleSupervisorProxy;
import cern.molr.server.supervisor.StatefulMoleSupervisorProxyNew;
import cern.molr.server.supervisor.SupervisorsManagerImpl;
import cern.molr.server.supervisor.SupervisorsManagerImplNew;
import cern.molr.type.Ack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cern.molr.commons.response.MissionCancelResponse.MissionCancelResponseFailure;
import cern.molr.commons.response.MissionCancelResponse.MissionCancelResponseSuccess;
import cern.molr.commons.response.MissionExecutionResponse.MissionExecutionResponseFailure;
import cern.molr.commons.response.MissionExecutionResponse.MissionExecutionResponseSuccess;
import cern.molr.commons.response.MissionGenericResponse.MissionGenericResponseFailure;
import cern.molr.commons.response.MissionGenericResponse.MissionGenericResponseSuccess;
import cern.molr.commons.response.SupervisorRegisterResponse.SupervisorRegisterResponseSuccess;
import cern.molr.commons.response.SupervisorRegisterResponse.SupervisorRegisterResponseFailure;
import cern.molr.exception.UnknownMissionException;
import cern.molr.server.request.MissionCancelRequest;
import cern.molr.server.request.MissionExecutionRequest;
import cern.molr.server.request.MissionResultRequest;
import cern.molr.commons.response.SupervisorUnregisterResponse.SupervisorUnregisterResponseSuccess;
import cern.molr.commons.response.SupervisorUnregisterResponse.SupervisorUnregisterResponseFailure;

/**
 * {@link RestController} for {@link ServerMain} spring application
 * 
 * @author nachivpn
 * @author yassine
 */
@RestController
public class ServerRestController {

    private final ServerRestExecutionService gateway;

    public ServerRestController(ServerRestExecutionService gateway) {
        this.gateway = gateway;
    }


    @RequestMapping(path = "/instantiate", method = RequestMethod.POST)
    public <I> CompletableFuture<MissionExecutionResponse> instantiateMission(@RequestBody MissionExecutionRequest<I> request) {
        return CompletableFuture.<MissionExecutionResponse>supplyAsync(()->{
            try {
                String mEId = newGateway.instantiate(request.getMissionDefnClassName(), request.getArgs());
                return new MissionExecutionResponseSuccess(new MissionExecutionResponseBean(mEId));
            } catch (UnknownMissionException|NoAppropriateSupervisorFound e) {
                return new MissionExecutionResponseFailure(e);
            }
        });
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public CompletableFuture<SupervisorRegisterResponse> register(@RequestBody SupervisorRegisterRequest request) {
        try {
            return CompletableFuture.<SupervisorRegisterResponse>supplyAsync(() ->{
                StatefulMoleSupervisor moleSupervisor = new StatefulMoleSupervisorProxy(request.getHost(),request.getPort());
                String id=supervisorsManager.addSupervisor(moleSupervisor,request.getAcceptedMissions());
                return new SupervisorRegisterResponseSuccess(new SupervisorRegisterResponseBean(id));
            }).exceptionally(SupervisorRegisterResponseFailure::new);
        } catch (Exception e) {
            return CompletableFuture.supplyAsync(() -> new SupervisorRegisterResponseFailure(e));
        }
    }

    //TODO should be removed
    @RequestMapping(path = "/unregister", method = RequestMethod.POST)
    public CompletableFuture<SupervisorUnregisterResponse> uNregister(@RequestBody SupervisorUnregisterRequest request) {
        try {
            return CompletableFuture.<SupervisorUnregisterResponse>supplyAsync(() ->{
                supervisorsManager.removeSupervisor(request.getId());
                return new SupervisorUnregisterResponseSuccess(new Ack("Supervisor unregistered successfully"));
            }).exceptionally(SupervisorUnregisterResponseFailure::new);
        } catch (Exception e) {
            return CompletableFuture.supplyAsync(() -> new SupervisorUnregisterResponseFailure(e));
        }
    }


}
