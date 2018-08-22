package cern.molr.commons.events;

import cern.molr.commons.api.response.MissionEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event sent back by the supervisor when there is an exception. It is sent using dynamic serialization.
 *
 * @author yassine-kr
 */
public class MissionExceptionEvent extends MissionEvent {

    public MissionExceptionEvent(@JsonProperty("success") boolean success, @JsonProperty("throwable") Throwable
            throwable) {
        super(success, throwable);
    }

    public MissionExceptionEvent(Throwable throwable) {
        super(false, throwable);
    }

    @Override
    public String toString() {
        return getThrowable().getClass().getName() + ": " + getThrowable().getMessage();
    }

}
