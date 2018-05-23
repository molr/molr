package cern.molr.commons.response;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseDeserializer;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Interface representing response to a register request
 *
 * @author yassine-kr
 */
@JsonDeserialize(using = SupervisorRegisterResponse.SupervisorRegisterResponseDeserializer.class)
public interface SupervisorRegisterResponse extends TryResponse<SupervisorRegisterResponseBean>{

    public static class SupervisorRegisterResponseDeserializer
            extends TryResponseDeserializer<SupervisorRegisterResponse>{

        @Override
        public Class<? extends SupervisorRegisterResponse> getSuccessDeserializer() {
            return SupervisorRegisterResponseSuccess.class;
        }

        @Override
        public Class<? extends SupervisorRegisterResponse> getFailureDeserializer() {
            return SupervisorRegisterResponseFailure.class;
        }
        
        
    }
    
    @JsonDeserialize(as = SupervisorRegisterResponseSuccess.class)
    public class SupervisorRegisterResponseSuccess extends TryResponseSuccess<SupervisorRegisterResponseBean>
            implements SupervisorRegisterResponse {
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public SupervisorRegisterResponseSuccess() {
            super(null);
        }
        
        /**
         * @param r
         */
        public SupervisorRegisterResponseSuccess(SupervisorRegisterResponseBean r) {
            super(r);
        }
    }
    
    @JsonDeserialize(as = SupervisorRegisterResponseFailure.class)
    public class SupervisorRegisterResponseFailure extends TryResponseFailure<SupervisorRegisterResponseBean>
            implements SupervisorRegisterResponse {

        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        /**
         */
        public SupervisorRegisterResponseFailure() {
            super(null);
        }
        /**
         * @param l
         */
        public SupervisorRegisterResponseFailure(Throwable l) {
            super(l);
        }
    }
    
}
