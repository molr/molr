package cern.molr.commons.response;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;
import cern.molr.type.Ack;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Interface representing response to an unregister request
 *
 * @author yassine
 */
public interface SupervisorUnregisterResponse extends TryResponse<Ack>{


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
