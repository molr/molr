/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.serialize;

import cern.molr.server.response.MissionExecutionResponse;
import cern.molr.server.response.MissionExecutionResponseFailure;
import cern.molr.server.response.MissionExecutionResponseSuccess;
import cern.molr.spring.util.TryResponseDeserializer;

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