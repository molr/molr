/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.request.client;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This request is sent by the client to MolR server to instantiate a mission
 *
 * @param <I> the mission arguments type
 *
 * @author yassine-kr
 */
public final class ServerInstantiationRequest<I> {

    private final String missionName;
    private final I missionArguments;

    public ServerInstantiationRequest(@JsonProperty("missionName") String missionName, @JsonProperty("missionArguments") I missionArguments) {
        this.missionName = missionName;
        this.missionArguments = missionArguments;
    }

    public String getMissionName() {
        return missionName;
    }

    public I getMissionArguments() {
        return missionArguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerInstantiationRequest request = (ServerInstantiationRequest) o;
        if (missionName != null ? !missionName.equals(request.missionName) : request.missionName != null) {
            return false;
        }
        return !(missionArguments != null ? !missionArguments.equals(request.missionArguments) :
                request.missionArguments != null);

    }

    @Override
    public int hashCode() {
        int result = missionName != null ? missionName.hashCode() : 0;
        result = 31 * result + (missionArguments != null ? missionArguments.hashCode() : 0);
        return result;
    }

}
