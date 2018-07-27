package cern.molr.commons.events;

import cern.molr.commons.api.response.MissionEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event sent back by the supervisor when the mission is finished
 *
 * @author yassine-kr
 */
public class MissionFinished<O> implements MissionEvent {
    private O result;

    public MissionFinished(@JsonProperty("result") O result) {
        this.result = result;
    }

    public O getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "MISSION FINISHED WITH THE RESULT " + result;
    }

}
