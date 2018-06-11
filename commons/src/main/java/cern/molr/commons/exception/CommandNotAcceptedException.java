package cern.molr.commons.exception;


/**
 * Thrown when the supervisor reject the execution of a command because of the current session state
 *
 * @author yassine-kr
 */
public class CommandNotAcceptedException extends Exception {

    private static final long serialVersionUID = 1L;

    public CommandNotAcceptedException(String message) {
        super(message);
    }

    public CommandNotAcceptedException(Throwable cause) {
        super(cause);
    }

    public CommandNotAcceptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
