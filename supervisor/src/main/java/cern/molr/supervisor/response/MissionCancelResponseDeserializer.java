/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.response;

import cern.molr.spring.util.TryResponseDeserializer;

public class MissionCancelResponseDeserializer extends TryResponseDeserializer<MissionCancelResponse>{

    @Override
    public Class<? extends MissionCancelResponse> getSuccessDeserializer() {
        return MissionCancelResponseSuccess.class;
    }

    @Override
    public Class<? extends MissionCancelResponse> getFailureDeserializer() {
        return MissionCancelResponseFailure.class;
    }

}
