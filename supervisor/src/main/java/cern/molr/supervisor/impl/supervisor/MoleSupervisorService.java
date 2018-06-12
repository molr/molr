package cern.molr.supervisor.impl.supervisor;

import cern.molr.commons.api.exception.ExecutionNotAcceptedException;
import cern.molr.commons.api.mission.Mission;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.SupervisorState;
import cern.molr.commons.events.MissionExceptionEvent;
import cern.molr.supervisor.SupervisorConfig;
import cern.molr.supervisor.api.supervisor.SupervisorSessionsManagerListener;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Arrays;

/**
 * Spring service representing a supervisor which manages its state and use it to decide whether it accepts a
 * mission or not
 *
 * @author yassine-kr
 */
@Service
public class MoleSupervisorService extends MoleSupervisorImpl {


    private SupervisorState supervisorState;
    private SupervisorConfig config;

    public MoleSupervisorService(SupervisorConfig config) {
        this.config = config;
        supervisorState = new SupervisorState(0, config.getMaxMissions());
        sessionsManager.addListener(new SupervisorSessionsManagerListener() {
            @Override
            public void onSessionAdded(String missionId) {
                supervisorState = new SupervisorState(sessionsManager.getSessionsNumber(), supervisorState
                        .getMaxMissions());
            }

            @Override
            public void onSessionRemoved(String missionId) {
                supervisorState = new SupervisorState(sessionsManager.getSessionsNumber(), supervisorState
                        .getMaxMissions());
            }
        });
    }

    @Override
    public SupervisorState getSupervisorState() {
        return supervisorState;
    }

    /**
     * synchronized method because it should use the supervisor supervisorState
     * to determine whether the instantiation is accepted
     *
     * @param mission
     * @param missionArguments
     * @param missionId
     * @param <I>
     *
     * @return a stream of events triggered by the mission execution
     */
    @Override
    synchronized public <I> Publisher<MissionEvent> instantiate(Mission mission, I missionArguments, String missionId) {
        try {
            accept(mission);
            return super.instantiate(mission, missionArguments, missionId);
        } catch (ExecutionNotAcceptedException e) {
            return Flux.create((FluxSink<MissionEvent> emitter) -> {
                emitter.next(new MissionExceptionEvent(e));
                emitter.complete();
            });
        }
    }

    /**
     * A method which verify whether a mission is accepted by the supervisor or not
     *
     * @param mission the mission to verify
     *
     * @throws ExecutionNotAcceptedException thrown when the mission is not accepted
     */
    private void accept(Mission mission) throws ExecutionNotAcceptedException {
        if (!Arrays.asList(config.getAcceptedMissions()).contains(mission.getMissionName())) {
            throw new ExecutionNotAcceptedException(
                    "Cannot accept execution of this mission: mission not accepted by the supervisor");
        }
        if (!supervisorState.isAvailable()) {
            throw new ExecutionNotAcceptedException(
                    "Cannot accept execution of this mission: the supervisor cannot execute more missions");
        }
    }
}
