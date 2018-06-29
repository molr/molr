package cern.molr.commons.events;

import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.MissionState;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An event wrapping a mission state. It is used by the communication between the MoleRunner and the supervisor, and
 * the communication between the supervisor and the MolR server.
 *
 * @author yassine-kr
 */
public class MissionStateEvent implements MissionEvent {
    private final MissionState state;

    public MissionStateEvent(@JsonProperty("state") MissionState state) {
        this.state = state;
    }

    public MissionState getState() {
        return state;
    }
}
