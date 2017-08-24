/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponseDeserializer;

@JsonDeserialize(using = MissionVoidResponse.MissionStringResponseDeserializer.class)
public interface MissionVoidResponse extends MissionGenericResponse<Void> {

    public static class MissionStringResponseDeserializer extends TryResponseDeserializer<MissionVoidResponse>{

        @Override
        public Class<? extends MissionVoidResponse> getSuccessDeserializer() {
            return MissionVoidResponseSuccess.class;
        }

        @Override
        public Class<? extends MissionVoidResponse> getFailureDeserializer() {
            return MissionVoidResponseFailure.class;
        }

    }

    @JsonDeserialize(as = MissionVoidResponseFailure.class)
    public class MissionVoidResponseFailure extends MissionGenericResponseFailure<Void> implements MissionVoidResponse {
        /**
         * Constructors are NOT needed for the this class because "we" DO NOT create objects in the code
         * The default constructor suffices for spring to de-serialize (or instantiate) the response object
         * (since TryResponse has setters and getters)
         */
    }

    @JsonDeserialize(as = MissionVoidResponseSuccess.class)
    public class MissionVoidResponseSuccess extends MissionGenericResponseSuccess<Void> implements MissionVoidResponse {
        /**
         * Constructors are NOT needed for the this class because "we" DO NOT create objects in the code
         * The default constructor suffices for spring to de-serialize (or instantiate) the response object
         * (since TryResponse has setters and getters)
         */
    }
}
