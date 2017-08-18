/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponseDeserializer;

@JsonDeserialize(using = MissionIntegerResponse.MissionIntegerResponseDeserializer.class)
public interface MissionIntegerResponse extends MissionGenericResponse<Integer> {
    
    public static class MissionIntegerResponseDeserializer extends TryResponseDeserializer<MissionIntegerResponse>{

        @Override
        public Class<? extends MissionIntegerResponse> getSuccessDeserializer() {
            return MissionIntegerResponseSuccess.class;
        }

        @Override
        public Class<? extends MissionIntegerResponse> getFailureDeserializer() {
            return MissionIntegerResponseFailure.class;
        }
        
    }
    
    @JsonDeserialize(as = MissionIntegerResponseSuccess.class)
    public class MissionIntegerResponseSuccess extends MissionGenericResponseSuccess<Integer> implements MissionIntegerResponse{
        /**
         * Constructors are NOT needed for the this class because "we" DO NOT create objects in the code
         * The default constructor suffices for spring to de-serialize (or instantiate) the response object
         * (since TryResponse has setters and getters)
         */
    }

    @JsonDeserialize(as = MissionIntegerResponseFailure.class)
    public class MissionIntegerResponseFailure extends MissionGenericResponseFailure<Integer> implements MissionIntegerResponse{
        /**
         * Constructors are NOT needed for the this class because "we" DO NOT create objects in the code
         * The default constructor suffices for spring to de-serialize (or instantiate) the response object
         * (since TryResponse has setters and getters)
         */
    }

    
}
