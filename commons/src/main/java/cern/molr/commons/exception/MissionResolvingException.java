package cern.molr.commons.exception;

/**
 * Thrown when there an error while attempting to resolve a mission class from a mission name
 *
 * @author yassine-kr
 */
public class MissionResolvingException extends Exception {

    private static final long serialVersionUID = 1L;

    public MissionResolvingException(String message) {
        super(message);
    }

    public MissionResolvingException(Throwable cause) {
        super(cause);
    }

    public MissionResolvingException(String message, Throwable cause) {
        super(message, cause);
    }
}
