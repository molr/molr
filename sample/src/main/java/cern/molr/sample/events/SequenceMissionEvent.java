package cern.molr.sample.events;

import cern.molr.commons.api.response.MissionEvent;
import cern.molr.sample.mole.SequenceMole;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An event triggered by the {@link SequenceMole}
 * @author yassine-kr
 */
public class SequenceMissionEvent implements MissionEvent {

    private final int taskNumber;
    private final Event event;

    public SequenceMissionEvent(@JsonProperty("taskNumer") int taskNumber, @JsonProperty("event") Event event) {
        this.taskNumber = taskNumber;
        this.event = event;
    }

    public int getTaskNumber() {
        return taskNumber;
    }

    public Event getEvent() {
        return event;
    }

    public enum Event {
        TASK_STARTED,
        TASK_FINISHED
    }
}