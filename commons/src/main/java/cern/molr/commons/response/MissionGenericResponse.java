/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;

public interface MissionGenericResponse<T> extends TryResponse<T> {

    /*
     * No de-serializer for this class, because.. well.. you cannot write one?
     */
    
    @JsonDeserialize(as = MissionGenericResponseSuccess.class)
    public class MissionGenericResponseSuccess<X> extends TryResponseSuccess<X> implements MissionGenericResponse<X>{
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public MissionGenericResponseSuccess() {
            super(null);
        }
        
        public MissionGenericResponseSuccess(X r) {
            super(r);
        }
    }
    
    @JsonDeserialize(as = MissionGenericResponseFailure.class)
    public class MissionGenericResponseFailure<X> extends TryResponseFailure<X> implements MissionGenericResponse<X>{
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public MissionGenericResponseFailure() {
            super(null);
        }
        
        public MissionGenericResponseFailure(Throwable l) {
            super(l);
        }
    }
    
}
