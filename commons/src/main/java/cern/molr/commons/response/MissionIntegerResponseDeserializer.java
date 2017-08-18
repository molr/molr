/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;

import cern.molr.commons.trye.TryResponseDeserializer;

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
