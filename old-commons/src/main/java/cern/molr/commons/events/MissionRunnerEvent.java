package cern.molr.commons.events;

import cern.molr.commons.api.response.MissionEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Kind of events which are sent back by the MoleRunner. It is a DTO class sent using dynamic serialization.
 * Wrapping one enum field could seem weird, why do not use an enum instead of a class. It would cause the
 * deserialization failing because the class name would not be saved in the JSON data when using dynamic serialization.
 *
 * @author yassine-kr
 */
public class MissionRunnerEvent implements MissionEvent {

    private final Event event;

    public MissionRunnerEvent(@JsonProperty("event") Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return event.toString();
    }

    public enum Event {
        MISSION_STARTED,
        SESSION_INSTANTIATED,
        SESSION_TERMINATED
    }
}