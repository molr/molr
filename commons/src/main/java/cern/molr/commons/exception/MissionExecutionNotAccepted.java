package cern.molr.commons.exception;
/**
 * Thrown when the MolR server or a supervisor does not accept to execute a mission; for example because of the mission
 * type or the fact that the max number of running missions is achieved
 * @author yassine-kr
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
