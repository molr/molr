package io.molr.mole.core.testing.strand;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.ImmutableList;

import io.molr.mole.core.tree.StrandExecutor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

/**
 * Simple class that keeps track of the exceptions published by a {@link StrandExecutor}
 */
public class StrandErrorsRecorder {

    private final AtomicReference<ImmutableList<Exception>> exceptions;
    private final ReplayProcessor<List<Exception>> errorsSink;
    private final Flux<List<Exception>> errorsStream;

    public StrandErrorsRecorder(StrandExecutor executor) {
        exceptions = new AtomicReference<>(ImmutableList.of());
        errorsSink = ReplayProcessor.cacheLast();
        errorsStream = errorsSink.publishOn(Schedulers.elastic());

        executor.getErrorsStream()
                .subscribe(e -> errorsSink.onNext(exceptions.updateAndGet(exceptions -> addTo(exceptions, e))));
    }

    private static ImmutableList<Exception> addTo(ImmutableList<Exception> currentExceptions, Exception newException) {
        return ImmutableList.<Exception>builder().addAll(currentExceptions).add(newException).build();
    }

    public List<Exception> getExceptions() {
        return exceptions.get();
    }

    public Flux<List<Exception>> getRecordedExceptionStream() {
        return errorsStream;
    }
}
