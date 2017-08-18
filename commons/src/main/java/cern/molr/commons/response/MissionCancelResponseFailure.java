/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.type.Ack;

@JsonDeserialize(as = MissionCancelResponseFailure.class)
public class MissionCancelResponseFailure extends TryResponseFailure<Ack> implements MissionCancelResponse{

    public MissionCancelResponseFailure() {
        super(null);
    }
    
    /**
     * @param l
     */
    public MissionCancelResponseFailure(Throwable l) {
        super(l);
    }

}
