package cern.molr.mole.spawner.debug;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;
import cern.molr.mole.supervisor.MoleExecutionResponseCommand;
import cern.molr.type.Ack;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A result to a command request
 */
public interface ResponseCommand extends TryResponse<Ack>,MoleExecutionResponseCommand {

    @JsonDeserialize(as = ResponseCommandSuccess.class)
    public static class ResponseCommandSuccess extends TryResponseSuccess<Ack> implements ResponseCommand {
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public ResponseCommandSuccess() {
            super(null);
        }

        public ResponseCommandSuccess(Ack r) {
            super(r);
        }
    }

    @JsonDeserialize(as = ResponseCommandFailure.class)
    public static class ResponseCommandFailure extends TryResponseFailure<Ack> implements ResponseCommand {
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public ResponseCommandFailure() {
            super(null);
        }

        public ResponseCommandFailure(Throwable l) {
            super(l);
        }
    }

}
