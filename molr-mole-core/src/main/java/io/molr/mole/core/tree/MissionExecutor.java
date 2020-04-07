package io.molr.mole.core.tree;

import io.molr.commons.domain.*;
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