package cern.molr.supervisor.impl;

import cern.molr.mission.Mission;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.spawner.run.RunSpawner;
import cern.molr.mole.supervisor.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

/**
 * Implementation of new interface {@link MoleSupervisorNew}
 * TODO remove "New" from class name
 * A proxy which communicate with remote supervisor
 * TODO implement methods
 * @author yassine
 */
public class MoleSupervisorProxyNew implements MoleSupervisorNew {

    @Override
    public <I> Flux<MoleExecutionEvent> instantiate(Mission mission, I args, String missionExecutionId) {
       return null;
    }

    @Override
    public Mono<MoleExecutionRequestCommandResult> instruct(MoleExecutionCommand command, String missionExecutionId) {
        return null;
    }
}
