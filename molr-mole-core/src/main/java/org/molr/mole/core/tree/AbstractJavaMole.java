package org.molr.mole.core.tree;

import org.molr.commons.domain.*;
import org.molr.mole.core.api.Mole;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class AbstractJavaMole implements Mole {

    private final Map<MissionHandle, MissionExecutor> executors = new ConcurrentHashMap<>();

    @Override
    public void instantiate(MissionHandle handle, Mission mission, Map<String, Object> params) {
        executors.put(handle, instantiate(mission, params));
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return fromExecutorOrError(handle, MissionExecutor::states);
    }

    @Override
    public Flux<MissionOutput> outputsFor(MissionHandle handle) {
        return fromExecutorOrError(handle, MissionExecutor::outputs);
    }


    private <T> Flux<T> fromExecutorOrError(MissionHandle handle, Function<MissionExecutor, Flux<T>> mapper) {
        return Optional.ofNullable(executors.get(handle))
                .map(mapper)
                .orElse(Flux.error(new IllegalStateException("No executor for handle '" + handle + "'")));
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        Optional.ofNullable(executors.get(handle))
                .ifPresent(e -> e.instruct(strand, command));
    }

    protected abstract MissionExecutor instantiate(Mission mission, Map<String, Object> params);

}
