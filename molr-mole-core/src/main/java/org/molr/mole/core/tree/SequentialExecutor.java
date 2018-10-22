package org.molr.mole.core.tree;

import org.molr.commons.domain.StrandCommand;

import java.util.concurrent.CompletableFuture;

public interface SequentialExecutor {

    void instruct(StrandCommand command);

    CompletableFuture<Void> end();

}
