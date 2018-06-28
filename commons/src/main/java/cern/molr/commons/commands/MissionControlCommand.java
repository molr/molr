package cern.molr.commons.commands;

import cern.molr.commons.api.request.MissionCommand;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A command which is intrpreted by the MoleRunner
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

    public enum Command {
        START,
        TERMINATE
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
}
