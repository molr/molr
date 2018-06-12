package cern.molr.commons.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A request sent to MolR server the forwarded to the supervisor to execute a command
 *
 * @author yassine-kr
 */
public final class MissionCommandRequest {

    private final String missionId;
    private final MissionCommand command;

    public MissionCommandRequest(@JsonProperty("missionId") String missionId, @JsonProperty("command")
            MissionCommand command) {
        this.missionId = missionId;
        this.command = command;
    }

    public String getMissionId() {
        return missionId;
    }

    public MissionCommand getCommand() {
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
        MissionCommandRequest request = (MissionCommandRequest) o;
        if (missionId != null ? !missionId.equals(request.missionId) : request.missionId != null) {
            return false;
        }
        return !(command != null ? !command.equals(request.command) : request.command != null);

    }

    @Override
    public int hashCode() {
        int result = missionId != null ? missionId.hashCode() : 0;
        result = 31 * result + (command != null ? command.hashCode() : 0);
        return result;
    }

}
