/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseDeserializer;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;

@JsonDeserialize(using = MissionObjectResponse.MissionObjectResponseDeserializer.class)
public interface MissionObjectResponse extends TryResponse<Object>{

    public static class MissionObjectResponseDeserializer extends TryResponseDeserializer<MissionObjectResponse>{

        @Override
        public Class<? extends MissionObjectResponse> getSuccessDeserializer() {
            return MissionObjectResponseSuccess.class;
        }

        @Override
        public Class<? extends MissionObjectResponse> getFailureDeserializer() {
            return MissionObjectResponseFailure.class;
        }

    }
    
    @JsonDeserialize(as = MissionObjectResponseFailure.class)
    public class MissionObjectResponseFailure extends TryResponseFailure<Object> implements MissionObjectResponse{
        /**
         * Constructors are NOT needed for the this class because "we" DO NOT create objects in the code
         * The default constructor suffices for spring to de-serialize (or instantiate) the response object
         * (since TryResponse has setters and getters)
         */
    }
    
    @JsonDeserialize(as = MissionObjectResponseSuccess.class)
    public class MissionObjectResponseSuccess extends TryResponseSuccess<Object> implements MissionObjectResponse{
        /**
         * Constructors are NOT needed for the this class because "we" DO NOT create objects in the code
         * The default constructor suffices for spring to de-serialize (or instantiate) the response object
         * (since TryResponse has setters and getters)
         */
    }
    
}

