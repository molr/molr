/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.rest.request;

public class MissionExecutionRequest<I> {

    private String missionDefnClassName;
    private I args;
    
    public MissionExecutionRequest(){}
    
    public MissionExecutionRequest(String missionDefnClassName, I args){
        this.missionDefnClassName = missionDefnClassName;
        this.args = args;
    }
    
    public String getMissionDefnClassName() {
        return missionDefnClassName;
    }
    public void setMissionDefnClassName(String missionDefnClassName) {
        this.missionDefnClassName = missionDefnClassName;
    }
    public I getArgs() {
        return args;
    }
    public void setArgs(I args) {
        this.args = args;
    }
    
}
