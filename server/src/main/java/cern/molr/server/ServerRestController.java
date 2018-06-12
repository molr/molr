/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server;

import cern.molr.commons.api.exception.ExecutionNotAcceptedException;
import cern.molr.commons.api.exception.NoSupervisorFoundException;
import cern.molr.commons.api.request.client.ServerInstantiationRequest;
import cern.molr.commons.api.request.supervisor.SupervisorRegisterRequest;
import cern.molr.commons.api.request.supervisor.SupervisorUnregisterRequest;
import cern.molr.commons.api.response.*;
import cern.molr.commons.api.response.InstantiationResponse.InstantiationResponseFailure;
import cern.molr.commons.api.response.InstantiationResponse.InstantiationResponseSuccess;
import cern.molr.commons.api.response.SupervisorRegisterResponse.SupervisorRegisterResponseSuccess;
import cern.molr.commons.api.response.SupervisorUnregisterResponse.SupervisorUnregisterResponseSuccess;
import cern.molr.commons.web.MolrConfig;
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


    @RequestMapping(path = MolrConfig.INSTANTIATE_PATH, method = RequestMethod.POST)
    public <I> Publisher<InstantiationResponse> instantiateMission(
            @RequestBody ServerInstantiationRequest<I> request) {

        return Mono.<InstantiationResponse>create((emitter) -> {
            try {
                String missionId = service.instantiate(request);
                emitter.success(new InstantiationResponseSuccess(new InstantiationResponseBean(missionId)));
            } catch (ExecutionNotAcceptedException | NoSupervisorFoundException e) {
                emitter.success(new InstantiationResponseFailure(e));
            }
        }).subscribeOn(Schedulers.fromExecutorService(executorService));
    }

    @RequestMapping(path = MolrConfig.REGISTER_PATH, method = RequestMethod.POST)
    public Publisher<SupervisorRegisterResponse> register(@RequestBody SupervisorRegisterRequest request) {
        return Mono.create((emitter) -> {
            String supervisorId = service.addSupervisor(request.getHost(), request.getPort(), request.getAcceptedMissions());
            emitter.success(new SupervisorRegisterResponseSuccess(new SupervisorRegisterResponseBean(supervisorId)));
        });
    }

    @RequestMapping(path = MolrConfig.UNREGISTER_PATH, method = RequestMethod.POST)
    public Publisher<SupervisorUnregisterResponse> uNregister(
            @RequestBody SupervisorUnregisterRequest request) {

        return Mono.create((emitter) -> {
            service.removeSupervisor(request.getSupervisorId());
            emitter.success(new SupervisorUnregisterResponseSuccess(new Ack("Supervisor unregistered successfully")));
        });
    }

}
