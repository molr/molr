/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.rest.result.serialize;

import cern.molr.rest.result.MissionExecutionResponse;
import cern.molr.rest.result.MissionExecutionResponseFailure;
import cern.molr.rest.result.MissionExecutionResponseSuccess;

public class MissionExecutionResponseDeserializer  extends TryResponseDeserializer<MissionExecutionResponse>{

    @Override
    public Class<? extends MissionExecutionResponse> getSuccessDeserializer() {
        return MissionExecutionResponseSuccess.class;
    }

    @Override
    public Class<? extends MissionExecutionResponse> getFailureDeserializer() {
        return MissionExecutionResponseFailure.class;
    }
    
    
}