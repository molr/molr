/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.rest.result;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.rest.result.serialize.MissionCancelResponseDeserializer;
import cern.molr.type.Ack;
import cern.molr.type.trye.TryResponse;

@JsonDeserialize(using = MissionCancelResponseDeserializer.class)
public interface MissionCancelResponse extends TryResponse<Ack>{

}
