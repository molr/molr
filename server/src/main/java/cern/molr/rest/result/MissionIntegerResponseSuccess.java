/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.rest.result;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = MissionIntegerResponseSuccess.class)
public class MissionIntegerResponseSuccess extends MissionXResponseSuccess<Integer> implements MissionIntegerResponse{

}
