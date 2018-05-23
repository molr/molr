package cern.molr.server.supervisor;

import cern.molr.mission.Mission;
import cern.molr.mole.supervisor.MissionCommandRequest;
import cern.molr.mole.supervisor.MoleExecutionCommand;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.server.StatefulMoleSupervisor;
import cern.molr.supervisor.impl.MoleSupervisorProxy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementation of a proxy MoleSupervisor which is able to return whether it is idle or not
 * @author yassine-kr
 */
public class StatefulMoleSupervisorProxy extends MoleSupervisorProxy implements StatefulMoleSupervisor {

    public StatefulMoleSupervisorProxy(String host, int port) {
        super(host, port);
    }

    @Override
    public <I> Flux<MoleExecutionEvent> instantiate(Mission mission, I args, String missionExecutionId) {
        return super.instantiate(mission,args,missionExecutionId);
    }

    @Override
    public Mono<MoleExecutionCommandResponse> instruct(MissionCommandRequest command) {
        return super.instruct(command);
    }

    @Override
    public boolean isIdle() {
        //TODO to implement
        return true;
    }
}
