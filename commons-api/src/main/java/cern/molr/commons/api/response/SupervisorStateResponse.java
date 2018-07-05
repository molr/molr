package cern.molr.commons.api.response;

import cern.molr.commons.api.type.trye.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response to a state request sent back by the supervisor
 *
 * @author yassine-kr
 */
public interface SupervisorStateResponse extends Try<SupervisorState> {

    class SupervisorStateResponseSuccess extends Success<SupervisorState>
            implements SupervisorStateResponse {

        public SupervisorStateResponseSuccess(@JsonProperty("success") SupervisorState supervisorState) {
            super(supervisorState);
        }
    }

    class SupervisorStateResponseFailure extends Failure<SupervisorState>
            implements SupervisorStateResponse {

        public SupervisorStateResponseFailure(@JsonProperty("throwable") Throwable throwable) {
            super(throwable);
        }
    }

}
