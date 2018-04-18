package cern.molr.mole.spawner.debug;

import cern.molr.commons.trye.TryResponse;
import cern.molr.commons.trye.TryResponseFailure;
import cern.molr.commons.trye.TryResponseSuccess;
import cern.molr.mole.supervisor.MoleExecutionRequestCommandResult;
import cern.molr.type.Ack;

/**
 * A result to a command request
 */
public interface RequestCommandResult extends TryResponse<Ack>,MoleExecutionRequestCommandResult {

    public class RequestCommandResultSuccess extends TryResponseSuccess<Ack> implements RequestCommandResult{
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public RequestCommandResultSuccess() {
            super(null);
        }

        public RequestCommandResultSuccess(Ack r) {
            super(r);
        }
    }

    public class RequestCommandResutFailure extends TryResponseFailure<Ack> implements RequestCommandResult{
        /**
         * Constructors are needed for this class because "we" create objects of this type in the code
         */
        public RequestCommandResutFailure() {
            super(null);
        }

        public RequestCommandResutFailure(Throwable l) {
            super(l);
        }
    }

}
