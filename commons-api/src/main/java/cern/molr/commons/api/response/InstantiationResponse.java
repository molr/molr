/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.response;


import cern.molr.commons.api.type.trye.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A response sent back to the client by the MolR server after receiving an instantiation request
 *
 * @author yassine-kr
 */
public interface InstantiationResponse extends Try<InstantiationResponseBean> {

    class InstantiationResponseSuccess extends Success<InstantiationResponseBean>
            implements InstantiationResponse {

        public InstantiationResponseSuccess(@JsonProperty("success") InstantiationResponseBean responseBean) {
            super(responseBean);
        }
    }

    class InstantiationResponseFailure extends Failure<InstantiationResponseBean>
            implements InstantiationResponse {

        public InstantiationResponseFailure(@JsonProperty("throwable") Throwable throwable) {
            super(throwable);
        }
    }

}
