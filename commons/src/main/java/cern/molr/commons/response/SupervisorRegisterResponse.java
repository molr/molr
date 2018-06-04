package cern.molr.commons.response;

import cern.molr.commons.type.trye.TryResponse;
import cern.molr.commons.type.trye.TryResponseFailure;
import cern.molr.commons.type.trye.TryResponseSuccess;

/**
 * Response to a register request sent back by the MolR server
 *
 * @author yassine-kr
 */
public interface SupervisorRegisterResponse extends TryResponse<SupervisorRegisterResponseBean> {


    class SupervisorRegisterResponseSuccess extends TryResponseSuccess<SupervisorRegisterResponseBean>
            implements SupervisorRegisterResponse {

        public SupervisorRegisterResponseSuccess() {
            super(null);
        }

        /**
         * @param responseBean
         */
        public SupervisorRegisterResponseSuccess(SupervisorRegisterResponseBean responseBean) {
            super(responseBean);
        }
    }

    class SupervisorRegisterResponseFailure extends TryResponseFailure<SupervisorRegisterResponseBean>
            implements SupervisorRegisterResponse {

        public SupervisorRegisterResponseFailure() {
            super(null);
        }

        /**
         * @param throwable
         */
        public SupervisorRegisterResponseFailure(Throwable throwable) {
            super(throwable);
        }
    }

}
