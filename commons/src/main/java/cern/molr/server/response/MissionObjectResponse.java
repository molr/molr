/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponse;

@JsonDeserialize(using = MissionObjectResponseDeserializer.class)
public interface MissionObjectResponse extends TryResponse<Object>{

}
