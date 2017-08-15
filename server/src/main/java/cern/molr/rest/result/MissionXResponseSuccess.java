/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.rest.result;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.type.trye.Success;

@JsonDeserialize(as = MissionXResponseSuccess.class)
public class MissionXResponseSuccess<X> extends Success<X> implements MissionXResponse<X>{

    public MissionXResponseSuccess() {
        super(null);
    }
    
    /**
     * @param r
     */
    public MissionXResponseSuccess(X r) {
        super(r);
    }

}
