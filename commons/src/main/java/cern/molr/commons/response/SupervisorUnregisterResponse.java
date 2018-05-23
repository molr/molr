package cern.molr.commons.response;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseDeserializer;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;
import cern.molr.type.Ack;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Interface representing response to an unregister request
 *
 * @author yassine-kr
 */
@JsonDeserialize(using = SupervisorUnregisterResponse.SupervisorUnregisterResponseDeserializer.class)
public interface SupervisorUnregisterResponse extends TryResponse<Ack>{

    public static class SupervisorUnregisterResponseDeserializer extends TryResponseDeserializer<SupervisorUnregisterResponse>{

        @Override
        public Class<? extends SupervisorUnregisterResponse> getSuccessDeserializer() {
            return SupervisorUnregisterResponseSuccess.class;
        }

        @Override
        public Class<? extends SupervisorUnregisterResponse> getFailureDeserializer() {
            return SupervisorUnregisterResponseFailure.class;
        }
        
        
    }
    
    @JsonDeserialize(as = SupervisorUnregisterResponseSuccess.class)
    public class SupervisorUnregisterResponseSuccess extends TryResponseSuccess<Ack> implements SupervisorUnregisterResponse {
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public SupervisorUnregisterResponseSuccess() {
            super(null);
        }
        
        /**
         * @param r
         */
        public SupervisorUnregisterResponseSuccess(Ack r) {
            super(r);
        }
    }
    
    @JsonDeserialize(as = SupervisorUnregisterResponseFailure.class)
    public class SupervisorUnregisterResponseFailure extends TryResponseFailure<Ack> implements SupervisorUnregisterResponse {

        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        /**
         */
        public SupervisorUnregisterResponseFailure() {
            super(null);
        }
        /**
         * @param l
         */
        public SupervisorUnregisterResponseFailure(Throwable l) {
            super(l);
        }
    }
    
}
