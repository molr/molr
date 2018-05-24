package cern.molr.commons.response;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Interface representing response to a register request
 *
 * @author yassine-kr
 */
public interface SupervisorRegisterResponse extends TryResponse<SupervisorRegisterResponseBean>{


    class SupervisorRegisterResponseSuccess extends TryResponseSuccess<SupervisorRegisterResponseBean> implements SupervisorRegisterResponse {

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

    class SupervisorRegisterResponseFailure extends TryResponseFailure<SupervisorRegisterResponseBean> implements SupervisorRegisterResponse {

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
