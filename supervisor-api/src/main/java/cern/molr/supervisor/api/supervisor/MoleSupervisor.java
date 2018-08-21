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
     * @return an events stream sent by the session controller. This stream contains mission events and mission states.
     */
    <I> Publisher<MissionEvent> instantiate(Mission mission, I missionArguments, String missionId);

    /**
     * Send commands to the MoleRunner
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

    /**
     * A method which should return the supervisor heartbeat
     *
     * @param interval the time interval between two states
     *
     * @return the stream of supervisor states
     */
    Publisher<SupervisorState> getHeartbeat(int interval);

}
