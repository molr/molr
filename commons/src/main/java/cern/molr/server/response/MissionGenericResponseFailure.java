/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponseFailure;

@JsonDeserialize(as = MissionGenericResponseFailure.class)
public class MissionGenericResponseFailure<X> extends TryResponseFailure<X> implements MissionGenericResponse<X>{

    public MissionGenericResponseFailure() {
        super(null);
    }
    
    /**
     * @param l
     */
    public MissionGenericResponseFailure(Throwable l) {
        super(l);
    }

}
