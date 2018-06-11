package cern.molr.commons.events;

import cern.molr.commons.response.MissionEvent;

/**
 * Event sent back by the supervisor when there is an exception
 *
 * @author yassine-kr
 */
public class MissionExceptionEvent implements MissionEvent {
    private Throwable throwable;
    private String message;

    public MissionExceptionEvent() {
    }

    public MissionExceptionEvent(String message) {
        this.message = message;
    }

    public MissionExceptionEvent(Throwable throwable) {
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
