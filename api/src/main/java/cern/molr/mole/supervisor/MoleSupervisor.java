package cern.molr.mole.supervisor;

import cern.molr.commons.SupervisorState;
import cern.molr.mission.Mission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * A supervisor allows to instantiate a mission, that means create a JVM where the requested mission will be executed.
 * It allows also to instruct the mission execution by sending commands. These are interpreted either by the JVM or
 * the mole itself.
 *
 * @author yassine-kr
 */
public interface MoleSupervisor {
    /**
     * Instantiate JVM which should execute the mission
     * @param mission
     * @param args
     * @param missionExecutionId
     * @param <I>
     * @return flux of events sent by controller of JVM
     */
    <I> Flux<MoleExecutionEvent> instantiate(Mission mission, I args, String missionExecutionId);

    /**
     * Send commands to JVM
     * @param commandRequest
     * @return
     */
    Mono<MoleExecutionCommandResponse> instruct(MissionCommandRequest commandRequest);

    /**
     * A method which should return the supervisor state
     * @return
     */
    SupervisorState getSupervisorState();

}
