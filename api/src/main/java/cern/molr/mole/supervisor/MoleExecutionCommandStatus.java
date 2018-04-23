package cern.molr.mole.supervisor;

/**
 * Event sent by JVM which tells to supervisor the status of the last command received
 * @author yassine
 */
public abstract class MoleExecutionCommandStatus implements MoleExecutionEvent{
    private boolean accepted;
    private String reason;

    public MoleExecutionCommandStatus() {
    }

    public MoleExecutionCommandStatus(boolean accepted, String reason) {
        this.accepted = accepted;
        this.reason = reason;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getReason() {
        return reason;
    }
}
