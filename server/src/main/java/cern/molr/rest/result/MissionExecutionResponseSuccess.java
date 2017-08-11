/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.rest.result;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.rest.bean.MissionExecutionResponseBean;
import cern.molr.type.trye.Success;

@JsonDeserialize(as = MissionExecutionResponseSuccess.class)
public class MissionExecutionResponseSuccess extends Success<MissionExecutionResponseBean> implements MissionExecutionResponse{

    public MissionExecutionResponseSuccess() {
        super(null);
    }
    
    /**
     * @param r
     */
    public MissionExecutionResponseSuccess(MissionExecutionResponseBean r) {
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
    public MissionExecutionResponseBean getResult() {
        return r;
    }

    @Override
    public void setResult(MissionExecutionResponseBean r){
        this.r = r;
    }

}
