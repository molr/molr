/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = MissionIntegerResponseSuccess.class)
public class MissionIntegerResponseSuccess extends MissionGenericResponseSuccess<Integer> implements MissionIntegerResponse{

}
