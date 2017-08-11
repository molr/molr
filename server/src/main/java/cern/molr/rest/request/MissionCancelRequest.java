/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.rest.request;

public class MissionCancelRequest {

    private String missionExecutionId;

    public MissionCancelRequest(){}
    
    public MissionCancelRequest(String missionExecutionId){
        this.setMissionExecutionId(missionExecutionId);
    }

    public String getMissionExecutionId() {
        return missionExecutionId;
    }

    public void setMissionExecutionId(String missionExecutionId) {
        this.missionExecutionId = missionExecutionId;
    }
    
}
