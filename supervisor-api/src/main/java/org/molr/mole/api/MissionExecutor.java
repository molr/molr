package org.molr.mole.api;

import org.molr.commons.api.domain.MissionCommand;
import org.molr.commons.api.domain.MissionHandle;
import org.molr.commons.api.domain.MissionState;
import org.molr.commons.api.domain.Strand;
import reactor.core.publisher.Flux;

public interface MissionExecutor {

    Flux<MissionState> states();

    void instruct(Strand strand, MissionCommand command);

}
