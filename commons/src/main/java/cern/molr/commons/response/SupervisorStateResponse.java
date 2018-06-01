package cern.molr.commons.response;

import cern.molr.commons.type.trye.TryResponse;
import cern.molr.commons.type.trye.TryResponseFailure;
import cern.molr.commons.type.trye.TryResponseSuccess;

/**
 * Response to a state request sent back by the supervisor
 *
 * @author yassine-kr
 */
public interface SupervisorStateResponse extends TryResponse<SupervisorState>{

    class SupervisorStateResponseSuccess extends TryResponseSuccess<SupervisorState>
            implements SupervisorStateResponse {

        public SupervisorStateResponseSuccess() {
            super(null);
        }
        
        /**
         * @param supervisorState
         */
        public SupervisorStateResponseSuccess(SupervisorState supervisorState) {
            super(supervisorState);
        }
    }

    class SupervisorStateResponseFailure extends TryResponseFailure<SupervisorState>
            implements SupervisorStateResponse {

        public SupervisorStateResponseFailure() {
            super(null);
        }
        /**
         * @param throwable
         */
        public SupervisorStateResponseFailure(Throwable throwable) {
            super(throwable);
        }
    }
    
}
