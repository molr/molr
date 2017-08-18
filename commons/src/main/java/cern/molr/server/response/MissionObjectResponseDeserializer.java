/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.response;

import cern.molr.commons.trye.TryResponseDeserializer;

public class MissionObjectResponseDeserializer extends TryResponseDeserializer<MissionObjectResponse>{

    @Override
    public Class<? extends MissionObjectResponse> getSuccessDeserializer() {
        return MissionObjectResponseSuccess.class;
    }

    @Override
    public Class<? extends MissionObjectResponse> getFailureDeserializer() {
        return MissionObjectResponseFailure.class;
    }

}
