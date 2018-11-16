package org.molr.mole.core.tree;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;
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
