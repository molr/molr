package cern.molr.commons.response;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;
import cern.molr.server.StatefulMoleSupervisor;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Interface representing a response to a state request sent to a supervisor
 *
 * @author yassine-kr
 */
public interface SupervisorStateResponse extends TryResponse<StatefulMoleSupervisor.State>{

    class SupervisorStateResponseSuccess extends TryResponseSuccess<StatefulMoleSupervisor.State>
            implements SupervisorStateResponse {

        public SupervisorStateResponseSuccess() {
            super(null);
        }
        
        /**
         * @param r
         */
        public SupervisorStateResponseSuccess(StatefulMoleSupervisor.State r) {
            super(r);
        }
    }

    class SupervisorStateResponseFailure extends TryResponseFailure<StatefulMoleSupervisor.State>
            implements SupervisorStateResponse {

        public SupervisorStateResponseFailure() {
            super(null);
        }
        /**
         * @param l
         */
        public SupervisorStateResponseFailure(Throwable l) {
            super(l);
        }
    }
    
}
