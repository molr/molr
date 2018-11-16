package cern.molr.commons.api.exception;

/**
 * Thrown when the MolR server or a supervisor does not accept to execute a mission; for example because of the mission
 * type or the fact that the max number of running missions was reached
 *
 * @author yassine-kr
 */
public class ExecutionNotAcceptedException extends Exception {

    private static final long serialVersionUID = 1L;

    public ExecutionNotAcceptedException(String message) {
        super(message);
    }

    public ExecutionNotAcceptedException(Throwable cause) {
        super(cause);
    }

    public ExecutionNotAcceptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
