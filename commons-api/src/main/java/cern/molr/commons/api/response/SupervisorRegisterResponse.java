package cern.molr.commons.api.response;

import cern.molr.commons.api.type.trye.TryResponse;
import cern.molr.commons.api.type.trye.TryResponseFailure;
import cern.molr.commons.api.type.trye.TryResponseSuccess;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response to a register request sent back by the MolR server
 *
 * @author yassine-kr
 */
public interface SupervisorRegisterResponse extends TryResponse<SupervisorRegisterResponseBean> {


    class SupervisorRegisterResponseSuccess extends TryResponseSuccess<SupervisorRegisterResponseBean>
            implements SupervisorRegisterResponse {

        public SupervisorRegisterResponseSuccess(@JsonProperty("success") SupervisorRegisterResponseBean responseBean) {
            super(responseBean);
        }
    }

    class SupervisorRegisterResponseFailure extends TryResponseFailure<SupervisorRegisterResponseBean>
            implements SupervisorRegisterResponse {

        public SupervisorRegisterResponseFailure(@JsonProperty("throwable") Throwable throwable) {
            super(throwable);
        }
    }

}
