/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponseSuccess;

@JsonDeserialize(as = MissionGenericResponseSuccess.class)
public class MissionGenericResponseSuccess<X> extends TryResponseSuccess<X> implements MissionGenericResponse<X>{

    public MissionGenericResponseSuccess() {
        super(null);
    }
    
    /**
     * @param r
     */
    public MissionGenericResponseSuccess(X r) {
        super(r);
    }

}
