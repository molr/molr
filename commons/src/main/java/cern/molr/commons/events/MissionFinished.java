package cern.molr.commons.events;

import cern.molr.commons.api.response.MissionEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event sent back by the supervisor when the mission is finished
 *
 * @author yassine-kr
 */
public class MissionFinished implements MissionEvent {
    private String missionName;
    private Object result;

    public MissionFinished(@JsonProperty("missionName") String missionName, @JsonProperty("result") Object result) {
        this.missionName = missionName;
        this.result = result;
    }

    public String getMissionName() {
        return missionName;
    }

    public Object getResult() {
        return result;
    }

}
