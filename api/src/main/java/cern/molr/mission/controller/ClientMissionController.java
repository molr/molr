/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mission.controller;

import cern.molr.mole.supervisor.MoleExecutionCommand;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controller used to control a mission execution
 * @author yassine-kr
 */
public interface ClientMissionController {

     Flux<MoleExecutionEvent> getFlux();

     Mono<MoleExecutionCommandResponse> instruct(MoleExecutionCommand command);
}
