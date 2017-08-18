/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponseSuccess;

@JsonDeserialize(as = MissionObjectResponseSuccess.class)
public class MissionObjectResponseSuccess extends TryResponseSuccess<Object> implements MissionObjectResponse{

    public MissionObjectResponseSuccess() {
        super(null);
    }
    
    /**
     * @param r
     */
    public MissionObjectResponseSuccess(Object r) {
        super(r);
    }

}
