package org.molr.mole.core.tree;

import org.molr.commons.domain.*;
import reactor.core.publisher.Flux;

public interface MissionExecutor {

    Flux<MissionState> states();

    Flux<MissionOutput> outputs();

    Flux<MissionRepresentation> representations();

    void instruct(Strand strand, StrandCommand command);

}
