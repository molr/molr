package cern.molr.commons.response;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;
import cern.molr.type.Ack;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Interface representing response to an unregister request
 *
 * @author yassine-kr
 */
public interface SupervisorUnregisterResponse extends TryResponse<Ack>{


    class SupervisorUnregisterResponseSuccess extends TryResponseSuccess<Ack>
            implements SupervisorUnregisterResponse {

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

    class SupervisorUnregisterResponseFailure extends TryResponseFailure<Ack>
            implements SupervisorUnregisterResponse {

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
