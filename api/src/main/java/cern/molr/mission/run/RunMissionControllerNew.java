/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mission.run;

import cern.molr.mole.supervisor.MoleExecutionCommand;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionResponseCommand;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controller used to control a mission execution
 * TODO remove "New" from class name
 * @author yassine
 */
public interface RunMissionControllerNew {

     Flux<MoleExecutionEvent> getFlux();

     Mono<MoleExecutionResponseCommand> instruct(MoleExecutionCommand command);
}
