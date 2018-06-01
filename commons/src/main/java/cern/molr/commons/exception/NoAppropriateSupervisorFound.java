package cern.molr.commons.exception;



/**
 * Thrown when the MolR server does not find an appropriate supervisor to execute a mission
 * @author yassine-kr
 */
public class NoAppropriateSupervisorFound extends Exception {

    private static final long serialVersionUID = 195586081128114794L;

    public NoAppropriateSupervisorFound(String message) {
        super(message);
    }

    public NoAppropriateSupervisorFound(Throwable cause) {
        super(cause);
    }

    public NoAppropriateSupervisorFound(String message, Throwable cause) {
        super(message, cause);
    }
}
