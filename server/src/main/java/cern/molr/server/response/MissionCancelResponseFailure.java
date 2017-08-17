/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.Failure;
import cern.molr.type.Ack;

@JsonDeserialize(as = MissionCancelResponseFailure.class)
public class MissionCancelResponseFailure extends Failure<Ack> implements MissionCancelResponse{

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
