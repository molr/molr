/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.server.serialize.MissionIntegerResponseDeserializer;

@JsonDeserialize(using = MissionIntegerResponseDeserializer.class)
public interface MissionIntegerResponse extends MissionXResponse<Integer> {
}
