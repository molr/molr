/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.request;

public class MissionExecutionRequest<I> {

    private String missionExecutionId;
    private String moleClassName;
    private String missionContentClassName;
    private I args;
    
    public MissionExecutionRequest(){}
    
    public MissionExecutionRequest(String missionExecutionId, String moleClassName, String missionContentClassName, I args){
        this.setMissionExecutionId(missionExecutionId);
        this.setMoleClassName(moleClassName);
        this.setMissionContentClassName(missionContentClassName);
        this.setArgs(args);
    }

    public String getMoleClassName() {
        return moleClassName;
    }

    public void setMoleClassName(String moleClassName) {
        this.moleClassName = moleClassName;
    }

    public String getMissionContentClassName() {
        return missionContentClassName;
    }

    public void setMissionContentClassName(String missionContentClassName) {
        this.missionContentClassName = missionContentClassName;
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
