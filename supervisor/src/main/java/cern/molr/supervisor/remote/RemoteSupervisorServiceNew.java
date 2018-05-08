package cern.molr.supervisor.remote;

import cern.molr.exception.MissionExecutionNotAccepted;
import cern.molr.mission.Mission;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.spawner.run.RunSpawner;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleSession;
import cern.molr.server.StatefulMoleSupervisorNew;
import cern.molr.supervisor.impl.MoleSupervisorImplNew;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Optional;

/**
 * Spring service which manages separate JVM.
 * It is stateful, it can tells whether is idle or not
 * @author yassine
 * TODO remove "New" from class name
 */
@Service
public class RemoteSupervisorServiceNew extends MoleSupervisorImplNew implements StatefulMoleSupervisorNew {

    @Override
    public Optional<State> getState() {
        //State example
        //TODO compute availability status using a specific algorithm
        State state=sessionsManager.getSessionsNumber()==0?new State(true,0):new State(false,sessionsManager.getSessionsNumber());
        return Optional.of(state);
    }

    /**
     * synchronized method because it should use the supervisor state to determine whether the instantiation is accepted
     * @param mission
     * @param args
     * @param missionExecutionId
     * @param <I>
     * @return
     */
    @Override
    synchronized public <I> Flux<MoleExecutionEvent> instantiate(Mission mission, I args, String missionExecutionId){
        try {
            accept(mission);
            return super.instantiate(mission,args,missionExecutionId);
        } catch (MissionExecutionNotAccepted e) {
            return Flux.create((FluxSink<MoleExecutionEvent> emitter)->{
                emitter.next(new RunEvents.MissionException(e));
                emitter.complete();
            });
        }
    }

    /**
     * Return whether the supervisor accepts a mission execution
     * @param mission
     * @return
     */
    private void accept(Mission mission) throws MissionExecutionNotAccepted{
        //TODO compute acceptance status using a specific algorithm
        boolean acceptance= getState().map(State::isAvailable).orElse(false);
        if(!acceptance)
            throw new MissionExecutionNotAccepted("Cannot accept execution of this mission");
    }
}
