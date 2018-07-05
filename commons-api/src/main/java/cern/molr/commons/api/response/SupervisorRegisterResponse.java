package cern.molr.commons.api.response;

import cern.molr.commons.api.type.trye.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response to a register request sent back by the MolR server
 *
 * @author yassine-kr
 */
public interface SupervisorRegisterResponse extends Try<SupervisorRegisterResponseBean> {


    class SupervisorRegisterResponseSuccess extends Success<SupervisorRegisterResponseBean>
            implements SupervisorRegisterResponse {

        public SupervisorRegisterResponseSuccess(@JsonProperty("success") SupervisorRegisterResponseBean responseBean) {
            super(responseBean);
        }
    }

    class SupervisorRegisterResponseFailure extends Failure<SupervisorRegisterResponseBean>
            implements SupervisorRegisterResponse {

        public SupervisorRegisterResponseFailure(@JsonProperty("throwable") Throwable throwable) {
            super(throwable);
        }
    }

}
