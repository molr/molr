package cern.molr.mole.supervisor;

/**
 * Event sent by JVM which tells to supervisor the status of the last received command
 * @author yassine-kr
 */
public abstract class MoleExecutionCommandStatus implements MoleExecutionEvent{
    private boolean accepted;
    private String reason;
    private Throwable exception;

    public MoleExecutionCommandStatus() {
    }

    public MoleExecutionCommandStatus(String reason) {
        this.reason = reason;
    }

    public MoleExecutionCommandStatus(boolean accepted, String reason) {
        this.accepted = accepted;
        this.reason = reason;
    }

    public MoleExecutionCommandStatus(Throwable exception) {
        accepted=false;
        this.exception = exception;
        reason=exception.getMessage();
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
