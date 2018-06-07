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
public class SupervisorInstantiationRequest<I> {

    private String missionExecutionId;
    private String missionName;
    private I args;

    public SupervisorInstantiationRequest() {
    }

    public SupervisorInstantiationRequest(String missionExecutionId,
                                          String missionName, I args) {
        this.setMissionExecutionId(missionExecutionId);
        this.setMissionName(missionName);
        this.setArgs(args);
    }


    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
    }

    public I getArgs() {
        return args;
    }

    public void setArgs(I args) {
        this.args = args;
    }

    public String getMissionExecutionId() {
        return missionExecutionId;
    }

    public void setMissionExecutionId(String missionExecutionId) {
        this.missionExecutionId = missionExecutionId;
    }

}
