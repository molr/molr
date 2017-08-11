/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.rest.result;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.rest.result.serialize.MissionIntegerResponseDeserializer;

@JsonDeserialize(using = MissionIntegerResponseDeserializer.class)
public interface MissionIntegerResponse extends MissionXResponse<Integer> {
}
