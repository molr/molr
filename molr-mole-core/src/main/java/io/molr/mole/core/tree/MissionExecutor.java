package io.molr.mole.core.tree;

import io.molr.commons.domain.BlockCommand;
import io.molr.commons.domain.MissionOutput;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.MissionState;
import io.molr.commons.domain.Strand;
import io.molr.commons.domain.StrandCommand;
import reactor.core.publisher.Flux;

public interface MissionExecutor {

    Flux<MissionState> states();

    Flux<MissionOutput> outputs();

    Flux<MissionRepresentation> representations();

    void instruct(Strand strand, StrandCommand command);

    void instructRoot(StrandCommand command);
    
    void instructBlock(String blockID, BlockCommand command);
    
    void dispose();

}