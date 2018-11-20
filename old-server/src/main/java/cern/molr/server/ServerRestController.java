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
import cern.molr.commons.web.MolrConfig;
import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    private final ServerExecutionService service;

    public ServerRestController(ExecutorService executorService, ServerExecutionService service) {
        this.executorService = executorService;
        this.service = service;
    }


    @CrossOrigin()
    @PostMapping(path = MolrConfig.INSTANTIATE_PATH)
    public <I> Publisher<InstantiationResponse> instantiateMission(
            @RequestBody ServerInstantiationRequest<I> request) {

        return Mono.<InstantiationResponse>create((emitter) -> {
            try {
                String missionId = service.instantiate(request);
                emitter.success(new InstantiationResponse(new InstantiationResponseBean(missionId)));
            } catch (ExecutionNotAcceptedException | NoSupervisorFoundException e) {
                emitter.success(new InstantiationResponse(e));
            }
        }).subscribeOn(Schedulers.fromExecutorService(executorService));
    }

    @PostMapping(path = MolrConfig.REGISTER_PATH)
    public Publisher<SupervisorRegisterResponse> register(@RequestBody SupervisorRegisterRequest request) {
        return Mono.create((emitter) -> {
            String supervisorId = service.addSupervisor(request.getHost(), request.getPort(), request.getAcceptedMissions());
            emitter.success(new SupervisorRegisterResponse(new SupervisorRegisterResponseBean(supervisorId)));
        });
    }

    @PostMapping(path = MolrConfig.UNREGISTER_PATH)
    public Publisher<SupervisorUnregisterResponse> uNregister(
            @RequestBody SupervisorUnregisterRequest request) {

        return Mono.create((emitter) -> {
            service.removeSupervisor(request.getSupervisorId());
            emitter.success(new SupervisorUnregisterResponse(new Ack("Supervisor unregistered successfully")));
        });
    }

}
