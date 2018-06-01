/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.response;


import cern.molr.commons.type.trye.TryResponse;
import cern.molr.commons.type.trye.TryResponseFailure;
import cern.molr.commons.type.trye.TryResponseSuccess;

/**
 * A response sent back to the client by the MolR server after receiving an instantiation request
 * @author yassine-kr
 */
public interface InstantiationResponse extends TryResponse<InstantiationResponseBean>{

    class InstantiationResponseSuccess extends TryResponseSuccess<InstantiationResponseBean>
            implements InstantiationResponse {

        public InstantiationResponseSuccess() {
            super(null);
        }
        
        /**
         * @param responseBean
         */
        public InstantiationResponseSuccess(InstantiationResponseBean responseBean) {
            super(responseBean);
        }
    }

    class InstantiationResponseFailure extends TryResponseFailure<InstantiationResponseBean>
            implements InstantiationResponse {

        public InstantiationResponseFailure() {
            super(null);
        }
        /**
         * @param throwable
         */
        public InstantiationResponseFailure(Throwable throwable) {
            super(throwable);
        }
    }
    
}
