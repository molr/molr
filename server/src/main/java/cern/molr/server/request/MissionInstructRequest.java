/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.request;

import cern.molr.mole.supervisor.MoleExecutionCommand;

/**
 * Request send by client to send a command
 */
public class MissionInstructRequest {

    private String missionExecutionId;
    private MoleExecutionCommand command;

    public MissionInstructRequest(){}

    public MissionInstructRequest(String missionExecutionId, MoleExecutionCommand command) {
        this.missionExecutionId = missionExecutionId;
        this.command = command;
    }

    public String getMissionExecutionId() {
        return missionExecutionId;
    }

    public void setMissionExecutionId(String missionExecutionId) {
        this.missionExecutionId = missionExecutionId;
    }

    public MoleExecutionCommand getCommand() {
        return command;
    }

    public void setCommand(MoleExecutionCommand command) {
        this.command = command;
    }
}
