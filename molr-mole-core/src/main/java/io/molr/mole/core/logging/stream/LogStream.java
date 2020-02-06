package io.molr.mole.core.logging.stream;

import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

// TODO Add javadoc
public class LogStream<T> {
    private ReplayProcessor<T> logsSink;
    private Flux<T> logsStream;

    public LogStream() {
        this.logsSink = ReplayProcessor.cacheLast();
        this.logsStream = logsSink.publishOn(Schedulers.elastic());
    }

    public void publish(T msg) {
        logsSink.onNext(msg);
    }

    public Flux<T> asStream() {
        return logsStream;
    }
}
