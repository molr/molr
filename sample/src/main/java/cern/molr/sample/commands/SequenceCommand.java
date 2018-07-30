package cern.molr.sample.commands;

import cern.molr.commons.api.request.MissionCommand;
import cern.molr.sample.mole.SequenceMole;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * A command for controlling a {@link SequenceMole}
 */
public class SequenceCommand implements MissionCommand {
    private final Command command;

    public SequenceCommand(@JsonProperty("command") Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SequenceCommand other = (SequenceCommand) o;
        return !(command != null ? !command.equals(other.command) : other.command != null);

    }

    @Override
    public String toString() {
        return command.toString();
    }

    public enum Command {
        STEP,//run one task
        SKIP,//skip the current task
        RESUME,//execute tasks automatically
        PAUSE//stop the automatic execution of tasks
    }
}
