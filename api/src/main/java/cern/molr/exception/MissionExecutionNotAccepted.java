package cern.molr.exception;
import cern.molr.mole.supervisor.MoleSupervisor;


/**
 * Thrown when the supervisor does not accept to execute a mission; for example because of the mission type or the fact that the max number of running missions is achieved
 * @author yassine
 */
public class MissionExecutionNotAccepted extends Exception {

    private static final long serialVersionUID = 195586081128114794L;

    public MissionExecutionNotAccepted(String message) {
        super(message);
    }

    public MissionExecutionNotAccepted(Throwable cause) {
        super(cause);
    }

    public MissionExecutionNotAccepted(String message, Throwable cause) {
        super(message, cause);
    }
}
