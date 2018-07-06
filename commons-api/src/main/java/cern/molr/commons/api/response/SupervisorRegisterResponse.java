package cern.molr.commons.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response to a registration request, sent back by the MolR server to the supervisor
 *
 * @author yassine-kr
 */
public final class SupervisorRegisterResponse extends Response<SupervisorRegisterResponseBean> {

    public SupervisorRegisterResponse(@JsonProperty("result") SupervisorRegisterResponseBean result, @JsonProperty("throwable") Throwable
            throwable, @JsonProperty("success") boolean success) {
        super(result, throwable, success);
    }

    public SupervisorRegisterResponse(SupervisorRegisterResponseBean result) {
        super(result);
    }

    public SupervisorRegisterResponse(Throwable throwable) {
        super(throwable);
    }

}
