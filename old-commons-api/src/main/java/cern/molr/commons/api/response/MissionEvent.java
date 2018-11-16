package cern.molr.commons.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

/**
 * Generic event triggered by a mission execution. An event can be an error an event or a normal event.
 *
 * @author yassine-kr
 */
public class MissionEvent {

    private final boolean success;
    private final Throwable throwable;

    public MissionEvent(@JsonProperty("success") boolean success, @JsonProperty("throwable") Throwable throwable) {
        this.success = success;
        this.throwable = throwable;
    }

    public boolean isSuccess() {
        return success;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Used for the Web Gui
     *
     * @return the string to be displayed by the web gui
     */
    @JsonProperty(access = READ_ONLY)
    public String getString() {
        return toString();
    }

}
