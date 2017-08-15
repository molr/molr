/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.type.Ack;
import cern.molr.type.trye.Success;

@JsonDeserialize(as = MissionCancelResponseSuccess.class)
public class MissionCancelResponseSuccess extends Success<Ack> implements MissionCancelResponse{

    public MissionCancelResponseSuccess() {
        super(null);
    }
    
    /**
     * @param r
     */
    public MissionCancelResponseSuccess(Ack r) {
        super(r);
    }

}
