/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.response;


import cern.molr.commons.api.type.trye.TryResponse;
import cern.molr.commons.api.type.trye.TryResponseFailure;
import cern.molr.commons.api.type.trye.TryResponseSuccess;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A response sent back to the client by the MolR server after receiving an instantiation request
 *
 * @author yassine-kr
 */
public interface InstantiationResponse extends TryResponse<InstantiationResponseBean> {

    class InstantiationResponseSuccess extends TryResponseSuccess<InstantiationResponseBean>
            implements InstantiationResponse {

        public InstantiationResponseSuccess(@JsonProperty("success") InstantiationResponseBean responseBean) {
            super(responseBean);
        }
    }

    class InstantiationResponseFailure extends TryResponseFailure<InstantiationResponseBean>
            implements InstantiationResponse {

        public InstantiationResponseFailure(@JsonProperty("throwable") Throwable throwable) {
            super(throwable);
        }
    }

}
