/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.rest.result;

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

    @Override
    public Exception getException() {
        return null;
    }

    @Override
    public void setException(Exception e) {
        return;
    }

    @Override
    public Ack getResult() {
        return this.r;
    }

    @Override
    public void setResult(Ack r) {
        this.r = r;
    }

}
