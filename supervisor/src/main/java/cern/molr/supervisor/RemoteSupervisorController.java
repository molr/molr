/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor;

import cern.molr.commons.request.server.SupervisorStateRequest;
import cern.molr.commons.response.SupervisorStateResponse;
import cern.molr.supervisor.impl.supervisor.MoleSupervisorService;
import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutorService;

/**
 * {@link RestController} for {@link RemoteSupervisorMain} spring application
 *
 * @author nachivpn
 * @author yassine-kr
 */
@RestController
public class RemoteSupervisorController {

    private final MoleSupervisorService moleSupervisorService;

    public RemoteSupervisorController(MoleSupervisorService service) {
        this.moleSupervisorService = service;
    }


    @RequestMapping(path = "/getState", method = RequestMethod.POST)
    public Publisher<SupervisorStateResponse> getState(@RequestBody SupervisorStateRequest request) {

        return Mono.create((emitter)->{
           emitter.success(new
                   SupervisorStateResponse.SupervisorStateResponseSuccess(moleSupervisorService.getSupervisorState()));
        });

    }

}
