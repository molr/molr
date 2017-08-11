/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.rest.result;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.type.Ack;
import cern.molr.type.trye.Failure;

@JsonDeserialize(as = MissionCancelResponseFailure.class)
public class MissionCancelResponseFailure extends Failure<Ack> implements MissionCancelResponse{

    public MissionCancelResponseFailure() {
        super(null);
    }
    
    /**
     * @param l
     */
    public MissionCancelResponseFailure(Exception l) {
        super(l);
    }

    @Override
    public Exception getException() {
        return this.l;
    }

    @Override
    public void setException(Exception e) {
        this.l = e;
    }

    @Override
    public Ack getResult() {
        return null;
    }

    @Override
    public void setResult(Ack r) {
        return;
    }

}
