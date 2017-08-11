/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.rest.result.serialize;

import cern.molr.rest.result.MissionCancelResponse;
import cern.molr.rest.result.MissionCancelResponseFailure;
import cern.molr.rest.result.MissionCancelResponseSuccess;

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
