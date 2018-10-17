package org.molr.mole.core.api;

import org.molr.commons.domain.MissionCommand;
import org.molr.commons.domain.MissionState;
import org.molr.commons.domain.Strand;
import reactor.core.publisher.Flux;

public interface MissionExecutor {

    Flux<MissionState> states();

    void instruct(Strand strand, MissionCommand command);

}
