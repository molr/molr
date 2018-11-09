package org.molr.mole.core.tree.tracking;

import org.molr.commons.domain.Result;
import reactor.core.publisher.Flux;

public interface BlockTracker<T> {

    Flux<T> asStream();

    T result();
}
