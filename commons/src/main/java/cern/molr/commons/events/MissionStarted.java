package cern.molr.commons.events;

import cern.molr.commons.response.MissionEvent;

/**
 * Event sent back by the supervisor when the mission is started
 * @author yassine-kr
 */
public class MissionStarted implements MissionEvent {
    private String missionClassName;
    private Object argument;
    private String moleClassName;

    public MissionStarted() {
    }

    public MissionStarted(String missionClassName, Object argument, String moleClassName) {
        this.missionClassName = missionClassName;
        this.argument = argument;
        this.moleClassName = moleClassName;
    }

    public String getMissionClassName() {
        return missionClassName;
    }

    public Object getArgument() {
        return argument;
    }

    public String getMoleClassName() {
        return moleClassName;
    }

}
