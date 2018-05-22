package cern.molr.commons.response;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;
import cern.molr.server.StatefulMoleSupervisor;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Interface representing a response to a state request sent to a supervisor
 *
 * @author yassine
 */
public interface SupervisorStateResponse extends TryResponse<StatefulMoleSupervisor.State>{

    public class SupervisorStateResponseSuccess extends TryResponseSuccess<StatefulMoleSupervisor.State> implements SupervisorStateResponse {
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
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

    public class SupervisorStateResponseFailure extends TryResponseFailure<StatefulMoleSupervisor.State> implements SupervisorStateResponse {

        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        /**
         */
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
