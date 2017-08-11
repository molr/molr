/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.rest.result.serialize;

import cern.molr.rest.result.MissionIntegerResponse;
import cern.molr.rest.result.MissionIntegerResponseFailure;
import cern.molr.rest.result.MissionIntegerResponseSuccess;

public class MissionIntegerResponseDeserializer extends TryResponseDeserializer<MissionIntegerResponse>{

    @Override
    public Class<? extends MissionIntegerResponse> getSuccessDeserializer() {
        return MissionIntegerResponseSuccess.class;
    }

    @Override
    public Class<? extends MissionIntegerResponse> getFailureDeserializer() {
        return MissionIntegerResponseFailure.class;
    }
    
}
