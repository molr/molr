package cern.molr.commons.events;

import cern.molr.commons.api.response.MissionEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event sent back by the supervisor when there is an exception. It is sent using dynamic serialization.
 *
 * @author yassine-kr
 */
public class MissionExceptionEvent implements MissionEvent {
    private final Throwable throwable;

    public MissionExceptionEvent(@JsonProperty("throwable") Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public String toString() {
        return throwable.getClass().getName() + ": " + throwable.getMessage();
    }

}
