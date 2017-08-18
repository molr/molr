/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponseFailure;

@JsonDeserialize(as = MissionObjectResponseFailure.class)
public class MissionObjectResponseFailure extends TryResponseFailure<Object> implements MissionObjectResponse{

    /**
     */
    public MissionObjectResponseFailure() {
        super(null);
    }
    /**
     * @param l
     */
    public MissionObjectResponseFailure(Throwable l) {
        super(l);
    }

}
