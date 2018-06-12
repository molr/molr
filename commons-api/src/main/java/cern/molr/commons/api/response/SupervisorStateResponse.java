package cern.molr.commons.api.response;

import cern.molr.commons.api.type.trye.TryResponse;
import cern.molr.commons.api.type.trye.TryResponseFailure;
import cern.molr.commons.api.type.trye.TryResponseSuccess;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response to a state request sent back by the supervisor
 *
 * @author yassine-kr
 */
public interface SupervisorStateResponse extends TryResponse<SupervisorState> {

    class SupervisorStateResponseSuccess extends TryResponseSuccess<SupervisorState>
            implements SupervisorStateResponse {

        public SupervisorStateResponseSuccess(@JsonProperty("success") SupervisorState supervisorState) {
            super(supervisorState);
        }
    }

    class SupervisorStateResponseFailure extends TryResponseFailure<SupervisorState>
            implements SupervisorStateResponse {

        public SupervisorStateResponseFailure(@JsonProperty("throwable") Throwable throwable) {
            super(throwable);
        }
    }

}
