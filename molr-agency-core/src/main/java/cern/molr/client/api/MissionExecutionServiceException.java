package cern.molr.client.api;

/**
 * An exception thrown by {@link MissionExecutionService}
 */
public class MissionExecutionServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    public MissionExecutionServiceException(String message) {
        super(message);
    }

    public MissionExecutionServiceException(Throwable cause) {
        super(cause);
    }

    public MissionExecutionServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
