package io.molr.mole.core.tree.tracking;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class BlockCombiner<T> implements BlockTracker<T> {

    private final Flux<T> summary;
    private final AtomicReference<T> result;

    private BlockCombiner(List<Flux<T>> inputs, T defaultValue, Function<Iterable<T>, T> summarizer) {
        this.result = new AtomicReference<>(defaultValue);
        summary = Flux.combineLatest(inputs, objects -> {
            List<T> childResults = stream(objects).map(o -> (T) o).collect(toList());
            return summarizer.apply(childResults);
        });
        summary.subscribe(this.result::set);
    }

    public static <T> BlockCombiner<T> combine(List<Flux<T>> inputs, T defaultValue, Function<Iterable<T>, T> summarizer) {
        return new BlockCombiner<T>(inputs, defaultValue, summarizer);
    }

    @Override
    public Flux<T> asStream() {
        return this.summary;
    }

    @Override
    public T result() {
        return this.result.get();
    }

}
