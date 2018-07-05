package cern.molr.commons.commands;

import cern.molr.commons.api.request.MissionCommand;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A command which is interpreted by the MoleRunner. It is a DTO class sent using dynamic serialization.
 * Wrapping one enum field could seem weird, why do not use an enum instead of a class. It would cause the
 * deserialization failing because the class name would not be saved in the JSON data when using dynamic serialization.
 *
 * @author yassine-kr
 */
public class MissionControlCommand implements MissionCommand {

    private final Command command;

    public MissionControlCommand(@JsonProperty("command") Command command) {
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
        MissionControlCommand other = (MissionControlCommand) o;
        return !(command != null ? !command.equals(other.command) : other.command != null);

    }

    @Override
    public String toString() {
        return command.toString();
    }

    public enum Command {
        START,
        TERMINATE
    }
}
