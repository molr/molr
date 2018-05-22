/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;

public interface MissionExecutionResponse extends TryResponse<MissionExecutionResponseBean>{

    public class MissionExecutionResponseSuccess extends TryResponseSuccess<MissionExecutionResponseBean> implements MissionExecutionResponse{
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

    public class MissionExecutionResponseFailure extends TryResponseFailure<MissionExecutionResponseBean> implements MissionExecutionResponse{
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
