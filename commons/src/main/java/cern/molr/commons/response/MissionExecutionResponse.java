/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseDeserializer;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;

@JsonDeserialize(using = MissionExecutionResponse.MissionExecutionResponseDeserializer.class)
public interface MissionExecutionResponse extends TryResponse<MissionExecutionResponseBean>{

    public static class MissionExecutionResponseDeserializer extends TryResponseDeserializer<MissionExecutionResponse>{

        @Override
        public Class<? extends MissionExecutionResponse> getSuccessDeserializer() {
            return MissionExecutionResponseSuccess.class;
        }

        @Override
        public Class<? extends MissionExecutionResponse> getFailureDeserializer() {
            return MissionExecutionResponseFailure.class;
        }
        
        
    }
    
    @JsonDeserialize(as = MissionExecutionResponseSuccess.class)
    public class MissionExecutionResponseSuccess extends TryResponseSuccess<MissionExecutionResponseBean>
            implements MissionExecutionResponse{
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public MissionExecutionResponseSuccess() {
            super(null);
        }
        
        /**
         * @param r
         */
        public MissionExecutionResponseSuccess(MissionExecutionResponseBean r) {
            super(r);
        }
    }
    
    @JsonDeserialize(as = MissionExecutionResponseFailure.class)
    public class MissionExecutionResponseFailure extends TryResponseFailure<MissionExecutionResponseBean>
            implements MissionExecutionResponse{
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        /**
         */
        public MissionExecutionResponseFailure() {
            super(null);
        }
        /**
         * @param l
         */
        public MissionExecutionResponseFailure(Throwable l) {
            super(l);
        }
    }
    
}
