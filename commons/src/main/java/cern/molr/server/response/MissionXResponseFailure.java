/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponseFailure;

@JsonDeserialize(as = MissionXResponseFailure.class)
public class MissionXResponseFailure<X> extends TryResponseFailure<X> implements MissionXResponse<X>{

    public MissionXResponseFailure() {
        super(null);
    }
    
    /**
     * @param l
     */
    public MissionXResponseFailure(Throwable l) {
        super(l);
    }

}
