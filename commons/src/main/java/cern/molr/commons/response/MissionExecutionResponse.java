/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;


import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;

/**
 * @author ?
 * @author yassine-kr
 */
public interface MissionExecutionResponse extends TryResponse<MissionExecutionResponseBean>{

    class MissionExecutionResponseSuccess extends TryResponseSuccess<MissionExecutionResponseBean>
            implements MissionExecutionResponse{

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

    class MissionExecutionResponseFailure extends TryResponseFailure<MissionExecutionResponseBean>
            implements MissionExecutionResponse{

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
