package cern.molr.commons.api.response;

import cern.molr.commons.api.type.trye.TryResponse;
import cern.molr.commons.api.type.trye.TryResponseFailure;
import cern.molr.commons.api.type.trye.TryResponseSuccess;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response to an unregistration request sent back by the MolR server
 *
 * @author yassine-kr
 */
public interface SupervisorUnregisterResponse extends TryResponse<Ack> {


    class SupervisorUnregisterResponseSuccess extends TryResponseSuccess<Ack>
            implements SupervisorUnregisterResponse {

        public SupervisorUnregisterResponseSuccess(@JsonProperty("success") Ack ack) {
            super(ack);
        }
    }

    class SupervisorUnregisterResponseFailure extends TryResponseFailure<Ack>
            implements SupervisorUnregisterResponse {

        public SupervisorUnregisterResponseFailure(@JsonProperty("throwable") Throwable throwable) {
            super(throwable);
        }
    }

}
