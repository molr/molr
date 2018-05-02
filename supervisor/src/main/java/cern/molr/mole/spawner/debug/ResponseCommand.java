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

    @JsonDeserialize(as = ResponseCommandResutFailure.class)
    public static class ResponseCommandResutFailure extends TryResponseFailure<Ack> implements ResponseCommand {
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public ResponseCommandResutFailure() {
            super(null);
        }

        public ResponseCommandResutFailure(Throwable l) {
            super(l);
        }
    }

}
