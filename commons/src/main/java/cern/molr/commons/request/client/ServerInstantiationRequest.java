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

    private String missionName;
    private I args;

    public ServerInstantiationRequest() {
    }

    public ServerInstantiationRequest(String missionName, I args) {
        this.missionName = missionName;
        this.args = args;
    }

    public String getMissionName() {
        return missionName;
    }

    public I getArgs() {
        return args;
    }


}
