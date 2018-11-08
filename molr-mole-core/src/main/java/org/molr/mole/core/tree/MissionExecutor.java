package org.molr.mole.core.tree;

import org.molr.commons.domain.MissionOutput;
import org.molr.commons.domain.StrandCommand;
import org.molr.commons.domain.MissionState;
import org.molr.commons.domain.Strand;
import reactor.core.publisher.Flux;

public interface MissionExecutor {

    Flux<MissionState> states();

    Flux<MissionOutput> outputs();

    void instruct(Strand strand, StrandCommand command);

}
