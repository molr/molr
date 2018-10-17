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

    /**
     * The events that are not sent back to the client are used only for changing the mole state
     */
    public enum Event {
        TASK_STARTED,//When a task is started
        TASK_FINISHED,//When a task is finished
        TASK_SKIPPED,//When a task is skipped, this event is not sent back to the client
        TASK_ERROR,//When an error is thrown by a task
        RESUMED,//When the automatic execution is started, it is not sent back to the client
        PAUSED//When the automatic execution is stopped, it is not sent back to the client
    }
}