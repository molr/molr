package cern.molr.commons.response;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.type.Ack;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public interface CommandResponse extends TryResponse<Ack>,MoleExecutionCommandResponse {

    @JsonDeserialize(as = CommandResponseSuccess.class)
    public static class CommandResponseSuccess extends TryResponseSuccess<Ack> implements CommandResponse {
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public CommandResponseSuccess() {
            super(null);
        }

        public CommandResponseSuccess(Ack r) {
            super(r);
        }
    }

    @JsonDeserialize(as = CommandResponseFailure.class)
    @JsonSerialize(as=CommandResponseFailure.class)
    public static class CommandResponseFailure extends TryResponseFailure<Ack> implements CommandResponse {
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public CommandResponseFailure() {
            super(null);
        }

        public CommandResponseFailure(Throwable l) {
            super(l);
        }
    }

}
