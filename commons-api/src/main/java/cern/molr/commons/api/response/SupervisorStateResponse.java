package cern.molr.commons.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response to a state request, sent back by the supervisor
 *
 * @author yassine-kr
 */
public final class SupervisorStateResponse extends Response<SupervisorState> {


    public SupervisorStateResponse(@JsonProperty("result") SupervisorState result, @JsonProperty("throwable") Throwable
            throwable, @JsonProperty("success") boolean success) {
        super(result, throwable, success);
    }

    public SupervisorStateResponse(SupervisorState result) {
        super(result);
    }

    public SupervisorStateResponse(Throwable throwable) {
        super(throwable);
    }
}
