package cern.molr.commons.request;

/**
 * A request sent to MolR server and a supervisor to execute a command
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

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public MissionCommand getCommand() {
        return command;
    }

    public void setCommand(MissionCommand command) {
        this.command = command;
    }
}
