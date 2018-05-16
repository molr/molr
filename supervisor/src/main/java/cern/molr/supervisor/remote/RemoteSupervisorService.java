package cern.molr.supervisor.remote;

import cern.molr.exception.MissionExecutionNotAccepted;
import cern.molr.mission.Mission;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.server.StatefulMoleSupervisor;
import cern.molr.supervisor.impl.MoleSupervisorImpl;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Optional;

/**
 * Spring service which manages a separate JVM
 * @author yassine
 */
@Service
public class RemoteSupervisorService extends MoleSupervisorImpl implements StatefulMoleSupervisor{

    @Override
    public Optional<StatefulMoleSupervisor.State> getState() {
        //State example
        //TODO compute availability status using a specific algorithm
        StatefulMoleSupervisor.State state=sessionsManager.getSessionsNumber()==0?new StatefulMoleSupervisor.State(true,0):new StatefulMoleSupervisor.State(false,sessionsManager.getSessionsNumber());
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
    @SuppressWarnings("unused")
    private void accept(Mission mission) throws MissionExecutionNotAccepted{
        //TODO compute acceptance status using a specific algorithm
        boolean acceptance= getState().map(StatefulMoleSupervisor.State::isAvailable).orElse(false);
        if(!acceptance)
            throw new MissionExecutionNotAccepted("Cannot accept execution of this mission");
    }
}
