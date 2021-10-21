package io.molr.mole.core.tree.executor;

import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;

public class CompletedState extends StrandExecutionState {

    public CompletedState(ConcurrentStrandExecutor context) {
        super(context);
    }

    @Override
    public void run() {
        /* Doing nothing, as we are at the end. */
    }

    @Override
    public void onEnterState() {
        context.updateStrandRunState(RunState.FINISHED);
    }

    @Override
    protected void executeCommand(StrandCommand command) {
        /* Ignored */
    }

}
