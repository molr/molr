package org.molr.mole.core.tree;

import org.molr.commons.domain.Result;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.molr.commons.domain.Result.UNDEFINED;

public class BlockResultCombiner implements BlockResultTracker {

    private final Flux<Result> summary;
    private final AtomicReference<Result> result = new AtomicReference<>(UNDEFINED);

    public BlockResultCombiner(List<Flux<Result>> inputs) {
        summary = Flux.combineLatest(inputs, objects -> {
            List<Result> childResults = stream(objects).map(o -> (Result) o).collect(toList());
            return Result.summaryOf(childResults);
        });
        summary.subscribe(this.result::set);
    }

    @Override
    public Flux<Result> asStream() {
        return this.summary;
    }

    @Override
    public Result result() {
        return this.result.get();
    }

}
