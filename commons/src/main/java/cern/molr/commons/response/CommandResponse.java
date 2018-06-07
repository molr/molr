package cern.molr.commons.response;

import cern.molr.commons.type.trye.TryResponse;
import cern.molr.commons.type.trye.TryResponseFailure;
import cern.molr.commons.type.trye.TryResponseSuccess;

import java.util.HashMap;
import java.util.Map;

/**
 * A command response sent back to the MolR server by the supervisor then to the client after sending a command request
 * It informs the client whether the command was accepted or not
 *
 * @author yassine-kr
 */
public interface CommandResponse extends TryResponse<Ack> {

    class CommandResponseSuccess extends TryResponseSuccess<Ack> implements CommandResponse {
        public CommandResponseSuccess() {
            super(null);
        }

        public CommandResponseSuccess(Ack ack) {
            super(ack);
        }
    }

    class CommandResponseFailure extends TryResponseFailure<Ack> implements CommandResponse{

        private String message;

        public CommandResponseFailure() {
            super(null);
        }

        public CommandResponseFailure(String message) {
            super(null);
            this.message = message;
        }

        public CommandResponseFailure(Throwable throwable) {
            super(throwable);
            this.message = throwable.getMessage();
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }

}
