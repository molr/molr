package cern.molr.commons.events;

import cern.molr.commons.api.response.MissionEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event sent back by the supervisor when the mission is started
 *
 * @author yassine-kr
 */
public class MissionStarted implements MissionEvent {
    private String missionName;
    private Object argument;
    private String moleClassName;

    public MissionStarted(@JsonProperty("missionName") String missionName, @JsonProperty("argument") Object argument,
                          @JsonProperty("moleClassName") String moleClassName) {
        this.missionName = missionName;
        this.argument = argument;
        this.moleClassName = moleClassName;
    }

    public String getMissionName() {
        return missionName;
    }

    public Object getArgument() {
        return argument;
    }

    public String getMoleClassName() {
        return moleClassName;
    }

}
