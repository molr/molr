/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.request.server;

/**
 * A request sent by the MolR server to supervisor to instantiate a mission
 * @author yassine-kr
 * @param <I> the arguments type
 */
public class SupervisorInstantiationRequest<I> {

    private String missionExecutionId;
    private String missionContentClassName;
    private I args;
    
    public SupervisorInstantiationRequest(){}
    
    public SupervisorInstantiationRequest(String missionExecutionId,
                                          String missionContentClassName, I args){
        this.setMissionExecutionId(missionExecutionId);
        this.setMissionContentClassName(missionContentClassName);
        this.setArgs(args);
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
