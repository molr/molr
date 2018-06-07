package cern.molr.commons.response;

import cern.molr.commons.type.trye.TryResponse;
import cern.molr.commons.type.trye.TryResponseFailure;
import cern.molr.commons.type.trye.TryResponseSuccess;

/**
 * Response to an unregistration request sent back by the MolR server
 *
 * @author yassine-kr
 */
public interface SupervisorUnregisterResponse extends TryResponse<Ack> {


    class SupervisorUnregisterResponseSuccess extends TryResponseSuccess<Ack>
            implements SupervisorUnregisterResponse {

        public SupervisorUnregisterResponseSuccess() {
            super(null);
        }

        /**
         * @param ack
         */
        public SupervisorUnregisterResponseSuccess(Ack ack) {
            super(ack);
        }
    }

    class SupervisorUnregisterResponseFailure extends TryResponseFailure<Ack>
            implements SupervisorUnregisterResponse {

        public SupervisorUnregisterResponseFailure() {
            super(null);
        }

        /**
         * @param throwable
         */
        public SupervisorUnregisterResponseFailure(Throwable throwable) {
            super(throwable);
        }
    }

}
