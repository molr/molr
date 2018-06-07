package cern.molr.commons.request;

/**
 * A request sent to MolR server the forwarded to the supervisor to execute a command
 *
 * @author yassine-kr
 */
public class MissionCommandRequest {
    private String missionId;
    private MissionCommand command;

    public MissionCommandRequest() {
    }

    public MissionCommandRequest(String missionId, MissionCommand command) {
        this.missionId = missionId;
        this.command = command;
    }

    public String getMissionId() {
        return missionId;
    }

    public MissionCommand getCommand() {
        return command;
    }

}
