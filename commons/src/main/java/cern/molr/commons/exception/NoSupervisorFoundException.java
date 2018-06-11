package cern.molr.commons.exception;


/**
 * Thrown when the MolR server does not find an appropriate supervisor to execute a mission
 *
 * @author yassine-kr
 */
public class NoSupervisorFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public NoSupervisorFoundException(String message) {
        super(message);
    }

    public NoSupervisorFoundException(Throwable cause) {
        super(cause);
    }

    public NoSupervisorFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
