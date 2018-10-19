package org.molr.mole.core.tree;

import org.molr.commons.domain.Result;
import reactor.core.publisher.Flux;

public interface BlockResultTracker {
    Flux<Result> asStream();

    Result result();
}
