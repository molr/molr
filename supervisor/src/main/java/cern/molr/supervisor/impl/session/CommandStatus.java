package cern.molr.supervisor.impl.session;

import cern.molr.commons.response.ManuallySerializable;
import cern.molr.commons.response.MissionEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Event sent by JVM which tells to supervisor the status of the last received command
 * @author yassine-kr
 */
public class CommandStatus implements MissionEvent,ManuallySerializable {
    private boolean accepted;
    private String reason;
    private Throwable exception;

    public CommandStatus() {
    }

    public CommandStatus(String reason) {
        this.reason = reason;
    }

    public CommandStatus(boolean accepted, String reason) {
        this.accepted = accepted;
        this.reason = reason;
    }

    public CommandStatus(Throwable exception) {
        accepted=false;
        this.exception = exception;
        reason=exception.getMessage();
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getReason() {
        return reason;
    }

    public Throwable getException() {
        return exception;
    }

    @Override
    public Map<String, String> getJsonMap() {
        Map<String,String> map=new HashMap<>();
        map.put("\"accepted\"",""+isAccepted()+"");
        map.put("\"reason\"","\""+getReason()+"\"");
        return map;
    }
}
