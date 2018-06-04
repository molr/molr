package cern.molr.commons.events;

import cern.molr.commons.response.MissionEvent;

/**
 * Event sent back by the supervisor when the mission is finished
 *
 * @author yassine-kr
 */
public class MissionFinished implements MissionEvent {
    private String missionClassName;
    private Object result;
    private String moleClassName;

    public MissionFinished() {
    }

    public MissionFinished(String missionClassName, Object result, String moleClassName) {
        this.missionClassName = missionClassName;
        this.result = result;
        this.moleClassName = moleClassName;
    }

    public String getMissionClassName() {
        return missionClassName;
    }

    public Object getResult() {
        return result;
    }

    public String getMoleClassName() {
        return moleClassName;
    }
}
