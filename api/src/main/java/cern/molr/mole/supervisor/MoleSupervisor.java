package cern.molr.mole.supervisor;

import cern.molr.mission.Mission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A supervisor
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

}
