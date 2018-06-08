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
import cern.molr.commons.response.SupervisorRegisterResponse.SupervisorRegisterResponseSuccess;
import cern.molr.commons.response.SupervisorUnregisterResponse.SupervisorUnregisterResponseSuccess;
import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
    public <I> Publisher<InstantiationResponse> instantiateMission(
            @RequestBody ServerInstantiationRequest<I> request) {

        return Mono.<InstantiationResponse>create((emitter)->{
            try {
                String mEId = service.instantiate(request);
                emitter.success(new InstantiationResponseSuccess(new InstantiationResponseBean(mEId)));
            } catch (MissionExecutionNotAccepted | NoAppropriateSupervisorFound e) {
                emitter.success(new InstantiationResponseFailure(e));
            }
        }).subscribeOn(Schedulers.fromExecutorService(executorService));
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public Publisher<SupervisorRegisterResponse> register(@RequestBody SupervisorRegisterRequest request) {
        return Mono.create((emitter) -> {
                String id = service.addSupervisor(request.getHost(), request.getPort(), request.getAcceptedMissions());
                emitter.success(new SupervisorRegisterResponseSuccess(new SupervisorRegisterResponseBean(id)));
            });
    }

    @RequestMapping(path = "/unregister", method = RequestMethod.POST)
    public Publisher<SupervisorUnregisterResponse> uNregister(
            @RequestBody SupervisorUnregisterRequest request) {

        return Mono.create((emitter) -> {
            service.removeSupervisor(request.getId());
            emitter.success(new SupervisorUnregisterResponseSuccess(new Ack("Supervisor unregistered successfully")));
        });
    }

}
