package cern.molr.commons.events;

import cern.molr.commons.response.MissionEvent;

/**
 * Event sent back by the supervisor when the mission is started
 *
 * @author yassine-kr
 */
public class MissionStarted implements MissionEvent {
    private String missionName;
    private Object argument;
    private String moleClassName;

    public MissionStarted() {
    }

    public MissionStarted(String missionName, Object argument, String moleClassName) {
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
