/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponseSuccess;

@JsonDeserialize(as = MissionExecutionResponseSuccess.class)
public class MissionExecutionResponseSuccess extends TryResponseSuccess<MissionExecutionResponseBean> implements MissionExecutionResponse{

    public MissionExecutionResponseSuccess() {
        super(null);
    }
    
    /**
     * @param r
     */
    public MissionExecutionResponseSuccess(MissionExecutionResponseBean r) {
        super(r);
    }

}
