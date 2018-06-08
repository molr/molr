/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.request.server;

/**
 * A request sent by the MolR server to supervisor to instantiate a mission
 *
 * @param <I> the arguments type
 *
 * @author yassine-kr
 */
public class InstantiationRequest<I> {

    private String missionExecutionId;
    private String missionName;
    private I args;

    public InstantiationRequest() {
    }

    public InstantiationRequest(String missionExecutionId, String missionName, I args) {
        this.missionExecutionId = missionExecutionId;
        this.missionName = missionName;
        this.args = args;
    }

    public String getMissionName() {
        return missionName;
    }

    public I getArgs() {
        return args;
    }

    public String getMissionExecutionId() {
        return missionExecutionId;
    }

}
