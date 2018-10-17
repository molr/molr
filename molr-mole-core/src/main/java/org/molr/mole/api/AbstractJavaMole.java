package org.molr.mole.api;

import org.molr.commons.api.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractJavaMole implements Supervisor {

    private final Map<MissionHandle, MissionExecutor> executors = new ConcurrentHashMap<>();

    @Override
    public void instantiate(MissionHandle handle, Mission mission, Map<String, Object> params) {
        executors.put(handle, instantiate(mission, params));
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return Optional.ofNullable(executors.get(handle))
                .map(e -> e.states())
                .orElse(Flux.error(new IllegalStateException("No executor for handle '" + handle + "'")));
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, MissionCommand command) {
        Optional.ofNullable(executors.get(handle))
                .ifPresent(e -> e.instruct(strand, command));
    }

    protected abstract MissionExecutor instantiate(Mission mission, Map<String, Object> params);

}
