package org.molr.mole.core.tree;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.StrandCommand;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface SequentialExecutor {

    void instruct(StrandCommand command);

    CompletableFuture<Void> end();


    /* All those will have to become fluxes */

    RunState runState();

    Block cursor() ;

    Set<StrandCommand> allowedCommands();

}
