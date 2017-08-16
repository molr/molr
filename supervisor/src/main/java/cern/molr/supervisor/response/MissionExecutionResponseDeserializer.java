/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.response;

import cern.molr.commons.serialize.TryResponseDeserializer;

public class MissionExecutionResponseDeserializer extends TryResponseDeserializer<MissionExecutionResponse>{

    @Override
    public Class<? extends MissionExecutionResponse> getSuccessDeserializer() {
        return MissionExecutionResponseSuccess.class;
    }

    @Override
    public Class<? extends MissionExecutionResponse> getFailureDeserializer() {
        return MissionExecutionResponseFailure.class;
    }

}
