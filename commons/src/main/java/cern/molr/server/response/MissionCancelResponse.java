/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponse;
import cern.molr.type.Ack;

@JsonDeserialize(using = MissionCancelResponseDeserializer.class)
public interface MissionCancelResponse extends TryResponse<Ack>{

}
