package cern.molr.mole.supervisor;

import cern.molr.exception.MissionExecutionNotAccepted;
import cern.molr.mission.Mission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * New interface for supervisors which suits new specifications
 * TODO remove word "New" from class name
 * @author yassine
 */
public interface MoleSupervisorNew {
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
     * Send commands to controller of JVM
     * @param command
     * @return
     */
    Mono<MoleExecutionResponseCommand> instruct(MoleExecutionCommand command);

}
