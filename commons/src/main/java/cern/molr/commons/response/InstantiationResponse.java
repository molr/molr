/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;


import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;

/**
 * A response sent back to the client by the MolR server after receiving an instantiation request
 * @author yassine-kr
 */
public interface MissionExecutionResponse extends TryResponse<MissionExecutionResponseBean>{

    class MissionExecutionResponseSuccess extends TryResponseSuccess<MissionExecutionResponseBean>
            implements MissionExecutionResponse{

        public MissionExecutionResponseSuccess() {
            super(null);
        }
        
        /**
         * @param responseBean
         */
        public MissionExecutionResponseSuccess(MissionExecutionResponseBean responseBean) {
            super(responseBean);
        }
    }

    class MissionExecutionResponseFailure extends TryResponseFailure<MissionExecutionResponseBean>
            implements MissionExecutionResponse{

        public MissionExecutionResponseFailure() {
            super(null);
        }
        /**
         * @param throwable
         */
        public MissionExecutionResponseFailure(Throwable throwable) {
            super(throwable);
        }
    }
    
}
