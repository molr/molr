package cern.molr.supervisor.remote;

import cern.molr.api.supervisor.SupervisorSessionsManagerListener;
import cern.molr.commons.events.MissionException;
import cern.molr.commons.response.SupervisorState;
import cern.molr.commons.exception.MissionExecutionNotAccepted;
import cern.molr.commons.mission.Mission;
import cern.molr.commons.response.MissionEvent;
import cern.molr.supervisor.impl.MoleSupervisorImpl;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Arrays;

/**
 * Spring service representing a supervisor which manages its state and use it to decide whether it accepts a
 * mission or not
 * @author yassine-kr
 */
@Service
public class MoleSupervisorService extends MoleSupervisorImpl {


    private SupervisorState supervisorState;
    private SupervisorConfig config;

    public MoleSupervisorService(SupervisorConfig config) {
        this.config = config;
        supervisorState =new SupervisorState();
        supervisorState.setNumMissions(0);
        supervisorState.setMaxMissions(config.getMaxMissions());
        sessionsManager.addListener(new SupervisorSessionsManagerListener() {
            @Override
            public void onSessionAdded(String missionId) {
                supervisorState.setNumMissions(sessionsManager.getSessionsNumber());
            }

            @Override
            public void onSessionRemoved(String missionId) {
                supervisorState.setNumMissions(sessionsManager.getSessionsNumber());
            }
        });
    }

    @Override
    public SupervisorState getSupervisorState() {
        return supervisorState;
    }

    /**
     * synchronized method because it should use the supervisor supervisorState to determine whether the instantiation is accepted
     * @param mission
     * @param args
     * @param missionExecutionId
     * @param <I>
     * @return
     */
    @Override
    synchronized public <I> Flux<MissionEvent> instantiate(Mission mission, I args, String missionExecutionId){
        try {
            accept(mission);
            return super.instantiate(mission,args,missionExecutionId);
        } catch (MissionExecutionNotAccepted e) {
            return Flux.create((FluxSink<MissionEvent> emitter)->{
                emitter.next(new MissionException(e));
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
        if(!supervisorState.isAvailable())
            throw new MissionExecutionNotAccepted(
                    "Cannot accept execution of this mission: the supervisor cannot execute more missions");
    }
}
