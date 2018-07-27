package cern.molr.supervisor.impl.session;

import cern.molr.commons.api.response.MissionEvent;


/**
 * Event sent by the MoleRunner which tells to the supervisor the status of the last received command
 *
 * @author yassine-kr
 */
public class CommandStatus implements MissionEvent {
    private boolean accepted;
    private String reason;
    private Throwable exception;

    public CommandStatus() {
    }

    public CommandStatus(String reason) {
        this.reason = reason;
    }

    public CommandStatus(boolean accepted, String reason) {
        this.accepted = accepted;
        this.reason = reason;
    }

    public CommandStatus(Throwable exception) {
        accepted = false;
        this.exception = exception;
        reason = exception.getMessage();
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getReason() {
        return reason;
    }

    public Throwable getException() {
        return exception;
    }

}
