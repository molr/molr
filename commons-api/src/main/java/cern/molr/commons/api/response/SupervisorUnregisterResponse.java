package cern.molr.commons.api.response;

import cern.molr.commons.api.type.trye.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response to an unregistration request sent back by the MolR server
 *
 * @author yassine-kr
 */
public interface SupervisorUnregisterResponse extends Try<Ack> {


    class SupervisorUnregisterResponseSuccess extends Success<Ack>
            implements SupervisorUnregisterResponse {

        public SupervisorUnregisterResponseSuccess(@JsonProperty("success") Ack ack) {
            super(ack);
        }
    }

    class SupervisorUnregisterResponseFailure extends Failure<Ack>
            implements SupervisorUnregisterResponse {

        public SupervisorUnregisterResponseFailure(@JsonProperty("throwable") Throwable throwable) {
            super(throwable);
        }
    }

}
