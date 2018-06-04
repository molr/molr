package cern.molr.commons.events;

import cern.molr.commons.response.ManuallySerializable;
import cern.molr.commons.response.MissionEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Event sent back by the supervisor when there is an exception
 *
 * @author yassine-kr
 */
public class MissionException implements MissionEvent, ManuallySerializable {
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

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return throwable.getClass().getName() + ": " + throwable.getMessage();
    }

    @Override
    public Map<String, String> getJsonMap() {
        Map<String, String> map = new HashMap<>();
        map.put("\"message\"", "\"" + message + "\"");
        return map;
    }
}
