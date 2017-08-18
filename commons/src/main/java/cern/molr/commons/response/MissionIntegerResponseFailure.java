/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = MissionIntegerResponseFailure.class)
public class MissionIntegerResponseFailure extends MissionGenericResponseFailure<Integer> implements MissionIntegerResponse{

}
