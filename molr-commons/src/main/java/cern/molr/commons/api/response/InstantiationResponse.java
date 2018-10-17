/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.response;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A response sent back to the client by the MolR server after receiving an instantiation request.
 *
 * @author yassine-kr
 */
public final class InstantiationResponse extends Response<InstantiationResponseBean> {


    public InstantiationResponse(@JsonProperty("result") InstantiationResponseBean result, @JsonProperty("throwable") Throwable
            throwable, @JsonProperty("success") boolean success) {
        super(result, throwable, success);
    }

    public InstantiationResponse(InstantiationResponseBean result) {
        super(result);
    }

    public InstantiationResponse(Throwable throwable) {
        super(throwable);
    }
}
