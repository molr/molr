package cern.molr.commons.api.response;

import cern.molr.commons.api.type.trye.TryResponse;
import cern.molr.commons.api.type.trye.TryResponseFailure;
import cern.molr.commons.api.type.trye.TryResponseSuccess;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A command response sent back to the MolR server by the supervisor then to the client after sending a command request
 * It informs the client whether the command was accepted or not
 *
 * @author yassine-kr
 */
public interface CommandResponse extends TryResponse<Ack> {

    class CommandResponseSuccess extends TryResponseSuccess<Ack> implements CommandResponse {

        public CommandResponseSuccess(@JsonProperty("success") Ack ack) {
            super(ack);
        }
    }

    class CommandResponseFailure extends TryResponseFailure<Ack> implements CommandResponse {

        public CommandResponseFailure(@JsonProperty("throwable") Throwable throwable) {
            super(throwable);
        }

    }

}
