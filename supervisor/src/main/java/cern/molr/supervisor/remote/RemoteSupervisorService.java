package cern.molr.supervisor.remote;

import cern.molr.exception.MissionExecutionNotAccepted;
import cern.molr.mission.Mission;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.SupervisorSessionsManagerListener;
import cern.molr.server.StatefulMoleSupervisor;
import cern.molr.supervisor.impl.MoleSupervisorImpl;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Arrays;
import java.util.Optional;

/**
 * Spring service which manages a separate JVM
 * @author yassine-kr
 */
@Service
public class RemoteSupervisorService extends MoleSupervisorImpl implements StatefulMoleSupervisor{


    private State state;
    private SupervisorConfig config;

    public RemoteSupervisorService(SupervisorConfig config) {
        this.config = config;
        state=new State();
        state.setNumMissions(0);
        state.setMaxMissions(config.getMaxMissions());
        sessionsManager.addListener(new SupervisorSessionsManagerListener() {
            @Override
            public void onSessionAdded(String missionId) {
                state.setNumMissions(sessionsManager.getSessionsNumber());
            }

            @Override
            public void onSessionRemoved(String missionId) {
                state.setNumMissions(sessionsManager.getSessionsNumber());
            }
        });
    }

    @Override
    public Optional<StatefulMoleSupervisor.State> getState() {
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
        if(!Arrays.asList(config.getAcceptedMissions()).contains(mission.getMissionDefnClassName()))
            throw new MissionExecutionNotAccepted(
                    "Cannot accept execution of this mission: mission not accepted by the supervisor");
        if(!state.isAvailable())
            throw new MissionExecutionNotAccepted(
                    "Cannot accept execution of this mission: the supervisor cannot execute more missions");
    }
}
