package org.molr.mole.core.tree.tracking;

import reactor.core.publisher.Flux;

public interface BlockTracker<T> {

    Flux<T> asStream();

    T result();
}
