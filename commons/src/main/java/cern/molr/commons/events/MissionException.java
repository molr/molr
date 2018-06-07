package cern.molr.commons.events;

import cern.molr.commons.response.MissionEvent;

/**
 * Event sent back by the supervisor when there is an exception
 *
 * @author yassine-kr
 */
public class MissionException implements MissionEvent {
    private Throwable throwable;
    private String message;

    public MissionException() {
    }

    public MissionException(String message) {
        this.message = message;
    }

    public MissionException(Throwable throwable) {
        this.throwable = throwable;
        this.message = throwable.getMessage();
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return throwable.getClass().getName() + ": " + throwable.getMessage();
    }

}
