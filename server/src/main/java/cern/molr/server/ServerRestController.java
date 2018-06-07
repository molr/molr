/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server;

import cern.molr.commons.exception.MissionExecutionNotAccepted;
import cern.molr.commons.exception.NoAppropriateSupervisorFound;
import cern.molr.commons.request.client.ServerInstantiationRequest;
import cern.molr.commons.request.supervisor.SupervisorRegisterRequest;
import cern.molr.commons.request.supervisor.SupervisorUnregisterRequest;
import cern.molr.commons.response.*;
import cern.molr.commons.response.InstantiationResponse.InstantiationResponseFailure;
import cern.molr.commons.response.InstantiationResponse.InstantiationResponseSuccess;
import cern.molr.commons.response.SupervisorRegisterResponse.SupervisorRegisterResponseFailure;
import cern.molr.commons.response.SupervisorRegisterResponse.SupervisorRegisterResponseSuccess;
import cern.molr.commons.response.SupervisorUnregisterResponse.SupervisorUnregisterResponseFailure;
import cern.molr.commons.response.SupervisorUnregisterResponse.SupervisorUnregisterResponseSuccess;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * {@link RestController} for {@link ServerMain} spring application
 *
 * @author nachivpn
 * @author yassine-kr
 */
@RestController
public class ServerRestController {

    private final ExecutorService executorService;

    private final ServerRestExecutionService service;

    public ServerRestController(ExecutorService executorService, ServerRestExecutionService service) {
        this.executorService = executorService;
        this.service = service;
    }


    @RequestMapping(path = "/instantiate", method = RequestMethod.POST)
    public <I> CompletableFuture<InstantiationResponse> instantiateMission(
            @RequestBody ServerInstantiationRequest<I> request) {
        return CompletableFuture.<InstantiationResponse>supplyAsync(() -> {
            try {
                String mEId = service.instantiate(request);
                return new InstantiationResponseSuccess(new InstantiationResponseBean(mEId));
            } catch (MissionExecutionNotAccepted | NoAppropriateSupervisorFound e) {
                return new InstantiationResponseFailure(e);
            }
        }, executorService);
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public CompletableFuture<SupervisorRegisterResponse> register(@RequestBody SupervisorRegisterRequest request) {
        try {
            return CompletableFuture.<SupervisorRegisterResponse>supplyAsync(() -> {
                String id = service.addSupervisor(request.getHost(), request.getPort(), request.getAcceptedMissions());
                return new SupervisorRegisterResponseSuccess(new SupervisorRegisterResponseBean(id));
            }).exceptionally(SupervisorRegisterResponseFailure::new);
        } catch (Exception error) {
            return CompletableFuture.supplyAsync(() -> new SupervisorRegisterResponseFailure(error));
        }
    }

    @RequestMapping(path = "/unregister", method = RequestMethod.POST)
    public CompletableFuture<SupervisorUnregisterResponse> uNregister(
            @RequestBody SupervisorUnregisterRequest request) {
        try {
            return CompletableFuture.<SupervisorUnregisterResponse>supplyAsync(() -> {
                service.removeSupervisor(request.getId());
                return new SupervisorUnregisterResponseSuccess(new Ack("Supervisor unregistered successfully"));
            }).exceptionally(SupervisorUnregisterResponseFailure::new);
        } catch (Exception error) {
            return CompletableFuture.supplyAsync(() -> new SupervisorUnregisterResponseFailure(error));
        }
    }


}
