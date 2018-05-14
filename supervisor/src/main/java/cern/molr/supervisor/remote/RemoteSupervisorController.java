/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.remote;

import cern.molr.commons.response.SupervisorStateResponse;
import cern.molr.supervisor.request.SupervisorStateRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * {@link RestController} for {@link RemoteSupervisorMain} spring application
 * 
 * @author nachivpn
 * @author yassine
 */
@RestController
public class RemoteSupervisorController{

    private final RemoteSupervisorService supervisorService;

    public RemoteSupervisorController(RemoteSupervisorService service,RemoteSupervisorService serviceNew) {
        this.supervisorService = service;
    }


    @RequestMapping(path = "/getState", method = RequestMethod.POST)
    public Future<? extends SupervisorStateResponse> getState(@RequestBody SupervisorStateRequest request) {
        return CompletableFuture.<SupervisorStateResponse>supplyAsync(()-> supervisorService.getState().<SupervisorStateResponse>map(SupervisorStateResponse.SupervisorStateResponseSuccess::new)
                .orElse(new SupervisorStateResponse.SupervisorStateResponseFailure(new Exception("unable to get the state from supervisor"))))
                .exceptionally(SupervisorStateResponse.SupervisorStateResponseFailure::new);
    }

}
