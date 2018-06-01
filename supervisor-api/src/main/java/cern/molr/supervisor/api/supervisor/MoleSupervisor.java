package cern.molr.supervisor.api.supervisor;

import cern.molr.commons.mission.Mission;
import cern.molr.commons.request.MissionCommandRequest;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.MissionEvent;
import cern.molr.commons.response.SupervisorState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A supervisor allows to instantiate a mission, that means create a Mole runner where the requested mission will
 * be executed. It allows also to instruct the mission execution by sending commands. These are interpreted either by
 * the Mole runner or the mole itself.
 *
 * @author yassine-kr
 */
public interface MoleSupervisor {
    /**
     * Create the mole runner which should execute the mission
     * @param mission
     * @param args
     * @param missionExecutionId
     * @param <I>
     * @return flux of events sent by the session controller
     */
    <I> Flux<MissionEvent> instantiate(Mission mission, I args, String missionExecutionId);

    /**
     * Send commands to the mole runner
     * @param commandRequest
     * @return
     */
    Mono<CommandResponse> instruct(MissionCommandRequest commandRequest);

    /**
     * A method which should return the supervisor state
     * @return
     */
    SupervisorState getSupervisorState();

}
