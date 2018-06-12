package cern.molr.supervisor.api.supervisor;

import cern.molr.commons.api.mission.Mission;
import cern.molr.commons.api.request.MissionCommandRequest;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.SupervisorState;
import org.reactivestreams.Publisher;

/**
 * A supervisor allows to instantiate a mission, that means create a MoleRunner where the requested mission will
 * be executed. It allows also to instruct the mission execution by sending commands. These are interpreted either by
 * the MoleRunner or the mole itself.
 *
 * @author yassine-kr
 */
public interface MoleSupervisor {
    /**
     * Create the MoleRunner which should execute the mission
     *
     * @param mission
     * @param missionArguments
     * @param missionId
     * @param <I>
     *
     * @return an events stream sent by the session controller
     */
    <I> Publisher<MissionEvent> instantiate(Mission mission, I missionArguments, String missionId);

    /**
     * Send commands to the MoleRunner
     *
     * @param commandRequest
     *
     * @return a stream of one element containing the command response
     */
    Publisher<CommandResponse> instruct(MissionCommandRequest commandRequest);

    /**
     * A method which should return the supervisor state
     *
     * @return the supervisor state
     */
    SupervisorState getSupervisorState();

}
