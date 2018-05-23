package cern.molr.mole.supervisor;

import cern.molr.mole.supervisor.MoleExecutionCommand;

/**
 * A request sent to MolR server and a supervisor to execute a command
 * @author yassine
 */
public class MissionCommandRequest {
    private String missionId;
    private MoleExecutionCommand command;

    public MissionCommandRequest() {
    }

    public MissionCommandRequest(String missionId, MoleExecutionCommand command) {
        this.missionId = missionId;
        this.command = command;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public MoleExecutionCommand getCommand() {
        return command;
    }

    public void setCommand(MoleExecutionCommand command) {
        this.command = command;
    }
}
