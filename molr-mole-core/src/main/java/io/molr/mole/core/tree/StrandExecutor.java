package io.molr.mole.core.tree;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.Strand;
import io.molr.commons.domain.StrandCommand;
import reactor.core.publisher.Flux;

import java.util.Set;

public interface StrandExecutor {

    void instruct(StrandCommand command);

    Flux<RunState> getStateStream();

    Flux<Block> getBlockStream();

    RunState getActualState();

    Block getActualBlock();

    Flux<Exception> getErrorsStream();

    Set<StrandCommand> getAllowedCommands();

    Strand getStrand();

}
