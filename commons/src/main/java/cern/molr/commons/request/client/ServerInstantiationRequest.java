/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.request.client;

/**
 * This request is sent by the client to MolR server to instantiate a mission
 *
 * @param <I> the arguments type
 *
 * @author yassine-kr
 */
public class ServerInstantiationRequest<I> {

    private String missionDefnClassName;
    private I args;

    public ServerInstantiationRequest() {
    }

    public ServerInstantiationRequest(String missionDefnClassName, I args) {
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
