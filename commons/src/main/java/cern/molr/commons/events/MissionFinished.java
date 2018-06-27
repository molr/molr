package cern.molr.commons.events;

import cern.molr.commons.api.response.MissionEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event sent back by the supervisor when the mission is finished
 *
 * @author yassine-kr
 */
public class MissionFinished<O> implements MissionEvent {
    private String missionName;
    private O result;

    public MissionFinished(@JsonProperty("missionName") String missionName, @JsonProperty("result") O result) {
        this.missionName = missionName;
        this.result = result;
    }

    public String getMissionName() {
        return missionName;
    }

    public O getResult() {
        return result;
    }

}
