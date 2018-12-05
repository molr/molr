package org.molr.mole.core.tree;

import org.molr.commons.domain.*;
import org.molr.mole.core.api.Mole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.molr.mole.core.utils.ThreadFactories.namedThreadFactory;

public abstract class AbstractJavaMole implements Mole {

    private final Map<MissionHandle, MissionExecutor> executors = new ConcurrentHashMap<>();
    private final MissionHandleFactory handleFactory = new AtomicIncrementMissionHandleFactory(this);
    private final ExecutorService moleExecutor = newSingleThreadExecutor(namedThreadFactory("local-agency-%d"));

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        return supplyAsync(() -> {
            MissionHandle handle = handleFactory.createHandle();
            executors.put(handle, executorFor(mission, params));
            return handle;
        });
    }


    @Override
    public final Flux<MissionState> statesFor(MissionHandle handle) {
        return fromExecutorOrError(handle, MissionExecutor::states);
    }

    @Override
    public final Flux<MissionOutput> outputsFor(MissionHandle handle) {
        return fromExecutorOrError(handle, MissionExecutor::outputs);
    }

    @Override
    public final Flux<MissionRepresentation> representationsFor(MissionHandle handle) {
        return fromExecutorOrError(handle, MissionExecutor::representations);
    }


    private <T> Flux<T> fromExecutorOrError(MissionHandle handle, Function<MissionExecutor, Flux<T>> mapper) {
        return Optional.ofNullable(executors.get(handle))
                .map(mapper)
                .orElse(Flux.error(new IllegalStateException("No executor for handle '" + handle + "'")));
    }

    @Override
    public final void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        Optional.ofNullable(executors.get(handle))
                .ifPresent(e -> e.instruct(strand, command));
    }

    public final void instructRoot(MissionHandle handle, StrandCommand command) {
        Optional.ofNullable(executors.get(handle))
                .ifPresent(e -> e.instructRoot(command));
    }

    private <T> Mono<T> supplyAsync(Supplier<T> supplier) {
        return Mono.fromFuture(CompletableFuture.supplyAsync(supplier, moleExecutor));
    }

    protected abstract MissionExecutor executorFor(Mission mission, Map<String, Object> params);


}
