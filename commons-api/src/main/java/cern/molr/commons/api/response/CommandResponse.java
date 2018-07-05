package cern.molr.commons.api.response;

import cern.molr.commons.api.type.trye.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A command response sent back to the MolR server by the supervisor then to the client after sending a command request
 * It informs the client whether the command was accepted or not
 *
 * @author yassine-kr
 */
public final class CommandResponse extends Response<Ack> {

    public CommandResponse(@JsonProperty("result") Ack result, @JsonProperty("throwable") Throwable
            throwable, @JsonProperty("success") boolean success) {
        super(result, throwable, success);
    }

    public CommandResponse(Ack result) {
        super(result);
    }

    public CommandResponse(Throwable throwable) {
        super(throwable);
    }

    @Override
    public String toString() {
        if (isSuccess())
            return "Command Accepted";
        else
            return "Command Rejected";
    }

}
