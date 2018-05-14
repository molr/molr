package cern.molr.server.supervisor;

import cern.molr.commons.response.SupervisorStateResponse;
import cern.molr.mission.Mission;
import cern.molr.mole.supervisor.MoleExecutionCommand;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.server.StatefulMoleSupervisor;
import cern.molr.supervisor.impl.MoleSupervisorProxy;
import cern.molr.supervisor.request.SupervisorStateRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Implementation of a proxy MoleSupervisor which is able to return whether it is idle or not
 * @author yassine
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
    public Mono<MoleExecutionCommandResponse> instruct(MoleExecutionCommand command) {
        return super.instruct(command);
    }


    //TODO when MolR server is unable to get the state of the supervisor, it should unregister it from supervisors manager
    @Override
    public Optional<State> getState() {
        try {
            return client.post("/getState",SupervisorStateRequest.class,new SupervisorStateRequest(),SupervisorStateResponse.class).get().match((throwable)->{
                throwable.printStackTrace();
                return Optional.empty();
            }, Optional::<State>ofNullable);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
