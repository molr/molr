package org.molr.mole.core.tree;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.Result;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.molr.commons.domain.Result.UNDEFINED;

public class LeafResultTracker implements BlockResultTracker {

    private final ReplayProcessor<Result> summary = ReplayProcessor.cacheLast();
    private final AtomicReference<Result> result = new AtomicReference<>(UNDEFINED);

    public LeafResultTracker() {
        summary.subscribe(this.result::set);
    }

    @Override
    public Flux<Result> asStream() {
        return this.summary;
    }

    public void push(Result result) {
        summary.onNext(result);
    }


    @Override
    public Result result() {
        return result.get();
    }
}
