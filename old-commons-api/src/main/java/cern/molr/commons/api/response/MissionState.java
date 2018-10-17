package cern.molr.commons.api.response;

import cern.molr.commons.api.request.MissionCommand;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A class representing a mission state generated during a mission execution and returned to the client.
 *
 * @author yassine-kr
 */
public class MissionState {

    private final Level level;
    private final String status;
    private final List<MissionCommand> possibleCommands;

    public MissionState(@JsonProperty("level") Level level, @JsonProperty("status") String status,
                        @JsonProperty("possibleCommands") List<MissionCommand> possibleCommands) {
        this.level = level;
        this.status = status;
        this.possibleCommands = possibleCommands;
    }

    public Level getLevel() {
        return level;
    }

    public String getStatus() {
        return status;
    }

    public List<MissionCommand> getPossibleCommands() {
        return possibleCommands;
    }

    @Override
    public String toString() {
        return level.toString() + " " + status + " " + possibleCommands;
    }

    public enum Level {
        MOLE_RUNNER,
        MOLE
    }
}
