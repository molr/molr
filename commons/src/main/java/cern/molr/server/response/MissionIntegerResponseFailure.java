/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = MissionIntegerResponseFailure.class)
public class MissionIntegerResponseFailure extends MissionXResponseFailure<Integer> implements MissionIntegerResponse{

}
