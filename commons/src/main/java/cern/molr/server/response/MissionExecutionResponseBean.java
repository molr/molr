/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.response;

public class MissionExecutionResponseBean {

    private String missionExecutionId;

    /**
     */
    public MissionExecutionResponseBean() {
    }
    
    /**
     * @param missionExecutionId
     */
    public MissionExecutionResponseBean(String missionExecutionId) {
        this.missionExecutionId = missionExecutionId;
    }

    public String getMissionExecutionId() {
        return missionExecutionId;
    }

    public void setMissionExecutionId(String missionExecutionId) {
        this.missionExecutionId = missionExecutionId;
    }

}
