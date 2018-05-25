/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.request;

/**
 * This request is sent by the client to MolR server to instantiate a mission
 * @author yassine-kr
 * @param <I> the arguments type
 */
public class ServerMissionExecutionRequest<I> {

    private String missionDefnClassName;
    private I args;
    
    public ServerMissionExecutionRequest(){}
    
    public ServerMissionExecutionRequest(String missionDefnClassName, I args){
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
