package cern.molr.commons.api.response;

import cern.molr.commons.api.type.trye.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A command response sent back to the MolR server by the supervisor then to the client after sending a command request
 * It informs the client whether the command was accepted or not
 *
 * @author yassine-kr
 */
public interface CommandResponse extends Try<Ack> {

    class CommandResponseSuccess extends Success<Ack> implements CommandResponse {

        public CommandResponseSuccess(@JsonProperty("success") Ack ack) {
            super(ack);
        }

        @Override
        public String toString() {
            return "Command Accepted";
        }
    }

    class CommandResponseFailure extends Failure<Ack> implements CommandResponse {

        public CommandResponseFailure(@JsonProperty("throwable") Throwable throwable) {
            super(throwable);
        }

        @Override
        public String toString() {
            return "Command Rejected";
        }
    }

}
