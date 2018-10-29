package org.molr.mole.core.tree;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.StrandCommand;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;

public interface StrandExecutor {
    CompletableFuture<Boolean> instruct(StrandCommand command);

    Flux<RunState> getStateStream();

    Flux<Block> getBlockStream();
}
