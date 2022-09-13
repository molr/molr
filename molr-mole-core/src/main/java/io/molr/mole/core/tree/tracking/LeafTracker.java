package io.molr.mole.core.tree.tracking;

import java.util.concurrent.atomic.AtomicReference;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class LeafTracker<T> implements BlockTracker<T> {

    private final Sinks.Many<T> summary = Sinks.many().replay().latest();
    private final AtomicReference<T> result;

    public LeafTracker(T initialValue) {
        this.result = new AtomicReference<>(initialValue);
        summary.asFlux().subscribe(this.result::set);
        summary.tryEmitNext(initialValue);
    }

    @Override
    public Flux<T> asStream() {
        return this.summary.asFlux();
    }

    public void push(T newResult) {
        summary.tryEmitNext(newResult);
    }


    @Override
    public T result() {
        return result.get();
    }
}
