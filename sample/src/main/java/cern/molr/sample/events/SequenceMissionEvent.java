package cern.molr.sample.events;

import cern.molr.commons.api.response.MissionEvent;
import cern.molr.sample.mole.SequenceMole;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An event triggered by the {@link SequenceMole}
 *
 * @author yassine-kr
 */
public class SequenceMissionEvent implements MissionEvent {

    private final int taskNumber;
    private final Event event;
    private final String message;

    public SequenceMissionEvent(@JsonProperty("taskNumber") int taskNumber, @JsonProperty("event") Event event,
                                @JsonProperty("message") String message) {
        this.taskNumber = taskNumber;
        this.event = event;
        this.message = message;
    }

    public int getTaskNumber() {
        return taskNumber;
    }

    public Event getEvent() {
        return event;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return event.toString() + " " + taskNumber + " " + message;
    }

    public enum Event {
        TASK_STARTED,
        TASK_FINISHED,
        TASK_SKIPPED,
        TASK_ERROR
    }
}