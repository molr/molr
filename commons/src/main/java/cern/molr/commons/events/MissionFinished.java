package cern.molr.commons.events;

import cern.molr.commons.api.response.MissionEvent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event sent back by the supervisor when a mission is finished. It is sent using dynamic serialization.
 *
 * @author yassine-kr
 */
public class MissionFinished<O> extends MissionEvent {
    private O result;

    public MissionFinished(@JsonProperty("success") boolean success, @JsonProperty("throwable") Throwable throwable,
                           @JsonProperty("result") O result) {
        super(success, throwable);
        this.result = result;
    }

    public MissionFinished(O result) {
        super(true, null);
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
