package org.molr.mole.core.tree.tracking;

import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.stream;

public class LeafTracker<T> implements BlockTracker<T> {

    private final ReplayProcessor<T> summary = ReplayProcessor.cacheLast();
    private final AtomicReference<T> result;

    public LeafTracker(T initialValue) {
        this.result = new AtomicReference<>(initialValue);
        summary.subscribe(this.result::set);
        summary.onNext(initialValue);
    }

    @Override
    public Flux<T> asStream() {
        return this.summary;
    }

    public void push(T result) {
        summary.onNext(result);
    }


    @Override
    public T result() {
        return result.get();
    }
}
