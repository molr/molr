/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.request.client;

/**
 * Request to get flux of events from the MolR server
 */
public class MissionEventsRequest {

    private String missionExecutionId;

    public MissionEventsRequest() {
    }

    public MissionEventsRequest(String missionExecutionId) {
        this.setMissionExecutionId(missionExecutionId);
    }

    public String getMissionExecutionId() {
        return missionExecutionId;
    }

    public void setMissionExecutionId(String missionExecutionId) {
        this.missionExecutionId = missionExecutionId;
    }

}
