package cern.molr.commons.response;

import cern.molr.commons.SupervisorState;
import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;

/**
 * Interface representing a response to a state request sent to a supervisor
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
