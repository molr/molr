/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.request.client;

/**
 * Request to get flux of events from the MolR server
 * @author yassine-kr
 */
public class MissionEventsRequest {

    private String missionExecutionId;

    public MissionEventsRequest() {
    }

    public MissionEventsRequest(String missionExecutionId) {
        this.missionExecutionId=missionExecutionId;
    }

    public String getMissionExecutionId() {
        return missionExecutionId;
    }

}
