package cern.molr.commons.exception;


/**
 * Thrown when the supervisor reject the execution of a command because of the current JVM state
 * @author yassine-kr
 */
public class CommandNotAcceptedException extends Exception {

    private static final long serialVersionUID = 195586081128114794L;

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
