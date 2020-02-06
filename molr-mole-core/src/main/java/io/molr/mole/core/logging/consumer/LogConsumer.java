package io.molr.mole.core.logging.consumer;

import reactor.core.publisher.Flux;

import java.util.Observer;

// TODO Add javadoc
public interface LogConsumer<U, V> extends Observer {
    Flux<U> asStream(V t);
}
