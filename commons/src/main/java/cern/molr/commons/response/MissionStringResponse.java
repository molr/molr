/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponseDeserializer;

@JsonDeserialize(using = MissionStringResponse.MissionStringResponseDeserializer.class)
public interface MissionStringResponse extends MissionGenericResponse<String> {

    public static class MissionStringResponseDeserializer extends TryResponseDeserializer<MissionStringResponse>{

        @Override
        public Class<? extends MissionStringResponse> getSuccessDeserializer() {
            return MissionStringResponseSuccess.class;
        }

        @Override
        public Class<? extends MissionStringResponse> getFailureDeserializer() {
            return MissionStringResponseFailure.class;
        }

    }

    @JsonDeserialize(as = MissionStringResponseFailure.class)
    public class MissionStringResponseFailure extends MissionGenericResponseFailure<String> implements MissionStringResponse {
        /**
         * Constructors are NOT needed for the this class because "we" DO NOT create objects in the code
         * The default constructor suffices for spring to de-serialize (or instantiate) the response object
         * (since TryResponse has setters and getters)
         */
    }

    @JsonDeserialize(as = MissionStringResponseSuccess.class)
    public class MissionStringResponseSuccess extends MissionGenericResponseSuccess<String> implements MissionStringResponse {
        /**
         * Constructors are NOT needed for the this class because "we" DO NOT create objects in the code
         * The default constructor suffices for spring to de-serialize (or instantiate) the response object
         * (since TryResponse has setters and getters)
         */
    }
}
