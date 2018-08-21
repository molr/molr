package cern.molr.supervisor.impl.session;

import cern.molr.commons.api.response.MissionEvent;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Event sent by the MoleRunner which tells to the supervisor the status of the last received command
 *
 * @author yassine-kr
 */
public class CommandStatus extends MissionEvent {
    private final String reason;

    public CommandStatus(@JsonProperty("success") boolean success, @JsonProperty("throwable") Throwable throwable,
                         @JsonProperty("reason") String reason) {
        super(success, throwable);
        this.reason = reason;
    }

    public CommandStatus(boolean success, String reason) {
        super(success, null);
        this.reason = reason;
    }

    public CommandStatus(Throwable throwable) {
        super(false, throwable);
        reason = throwable.getMessage();
    }

    public String getReason() {
        return reason;
    }

    public String toString() {
        return isSuccess() ? "command accepted: " + reason : "command not accepted: " + reason + " " +
                "throwable:" + getThrowable().getClass().getName() + ": " + getThrowable().getMessage();
    }

}
