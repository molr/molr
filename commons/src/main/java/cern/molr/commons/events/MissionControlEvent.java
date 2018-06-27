package cern.molr.commons.events;

import cern.molr.commons.api.response.MissionEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Kind of events which are sent back by the MoleRunner
 * @author yassine-kr
 */
public class MissionControlEvent implements MissionEvent {

    private final Event event;

    public MissionControlEvent(@JsonProperty("event") Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public enum Event {
        MISSION_STARTED,
        SESSION_INSTANTIATED,
        SESSION_TERMINATED
    }
}