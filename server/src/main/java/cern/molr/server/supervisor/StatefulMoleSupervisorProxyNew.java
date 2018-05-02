package cern.molr.server.supervisor;

import cern.molr.mission.Mission;
import cern.molr.mission.step.StepResult;
import cern.molr.mole.supervisor.MoleExecutionCommand;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionResponseCommand;
import cern.molr.mole.supervisor.MoleSupervisor;
import cern.molr.server.StatefulMoleSupervisor;
import cern.molr.server.StatefulMoleSupervisorNew;
import cern.molr.supervisor.impl.MoleSupervisorProxy;
import cern.molr.supervisor.impl.MoleSupervisorProxyNew;
import cern.molr.supervisor.request.MissionExecutionRequest;
import cern.molr.type.Ack;
import cern.molr.type.either.Either;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * Implementation of a proxy MoleSupervisor which is able to return whether it is idle or not based on the state of its completableFuture
 * a better solution would be to ask {@link MoleSupervisor server}
 * TODO remove "New" from class name
 * @author yassine
 */
public class StatefulMoleSupervisorProxyNew extends MoleSupervisorProxyNew implements StatefulMoleSupervisorNew {

    public StatefulMoleSupervisorProxyNew(String host, int port) {
        super(host, port);
    }

    @Override
    public <I> Flux<MoleExecutionEvent> instantiate(Mission mission, I args, String missionExecutionId) {
        return super.instantiate(mission,args,missionExecutionId);
    }

    @Override
    public Mono<MoleExecutionResponseCommand> instruct(MoleExecutionCommand command) {
        return super.instruct(command);
    }

    @Override
    public boolean isIdle() {
        //TODO to implement
        return true;
    }
}
