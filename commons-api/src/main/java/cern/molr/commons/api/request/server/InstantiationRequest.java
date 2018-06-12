/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.request.server;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A request sent by the MolR server to supervisor to instantiate a mission
 *
 * @param <I> the arguments type
 *
 * @author yassine-kr
 */
public final class InstantiationRequest<I> {

    private final String missionId;
    private final String missionName;
    private final I missionArguments;

    public InstantiationRequest(@JsonProperty("missionId") String missionId, @JsonProperty("missionName") String
            missionName, @JsonProperty("missionArguments") I missionArguments) {
        this.missionId = missionId;
        this.missionName = missionName;
        this.missionArguments = missionArguments;
    }

    public String getMissionName() {
        return missionName;
    }

    public I getMissionArguments() {
        return missionArguments;
    }

    public String getMissionId() {
        return missionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InstantiationRequest request = (InstantiationRequest) o;
        if (missionId != null ? !missionId.equals(request.missionId) : request.missionId != null) {
            return false;
        }

        if (missionName != null ? !missionName.equals(request.missionName) : request.missionName != null) {
            return false;
        }

        return !(missionArguments != null ? !missionArguments.equals(request.missionArguments) :
                request.missionArguments != null);

    }

    @Override
    public int hashCode() {
        int result = missionId != null ? missionId.hashCode() : 0;
        result = 31 * result + (missionName != null ? missionName.hashCode() : 0);
        result = 31 * result + (missionArguments != null ? missionArguments.hashCode() : 0);
        return result;
    }

}
