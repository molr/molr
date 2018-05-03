package cern.molr.commons.response;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseDeserializer;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;
import cern.molr.server.StatefulMoleSupervisorNew;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Interface representing a response to a state request sent to a supervisor
 *
 * @author yassine
 */
@JsonDeserialize(using = SupervisorStateResponse.SupervisorStateResponseDeserializer.class)
public interface SupervisorStateResponse extends TryResponse<StatefulMoleSupervisorNew.State>{

    public static class SupervisorStateResponseDeserializer extends TryResponseDeserializer<SupervisorStateResponse>{

        @Override
        public Class<? extends SupervisorStateResponse> getSuccessDeserializer() {
            return SupervisorStateResponseSuccess.class;
        }

        @Override
        public Class<? extends SupervisorStateResponse> getFailureDeserializer() {
            return SupervisorStateResponseFailure.class;
        }
        
        
    }
    
    @JsonDeserialize(as = SupervisorStateResponseSuccess.class)
    public class SupervisorStateResponseSuccess extends TryResponseSuccess<StatefulMoleSupervisorNew.State> implements SupervisorStateResponse {
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public SupervisorStateResponseSuccess() {
            super(null);
        }
        
        /**
         * @param r
         */
        public SupervisorStateResponseSuccess(StatefulMoleSupervisorNew.State r) {
            super(r);
        }
    }
    
    @JsonDeserialize(as = SupervisorStateResponseFailure.class)
    public class SupervisorStateResponseFailure extends TryResponseFailure<StatefulMoleSupervisorNew.State> implements SupervisorStateResponse {

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
