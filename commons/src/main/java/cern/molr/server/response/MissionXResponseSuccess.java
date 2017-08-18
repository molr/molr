/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponseSuccess;

@JsonDeserialize(as = MissionXResponseSuccess.class)
public class MissionXResponseSuccess<X> extends TryResponseSuccess<X> implements MissionXResponse<X>{

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
