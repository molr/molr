package io.molr.mole.core.tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.molr.commons.domain.*;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.logging.consumer.LogConsumer;
import io.molr.mole.core.logging.consumer.LogConsumerFactory;
import io.molr.mole.core.logging.consumer.RunningMissionLogConsumer;
import io.molr.mole.core.utils.ThreadFactories;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public abstract class AbstractJavaMole implements Mole {

    private final ReplayProcessor<AgencyState> statesSink = ReplayProcessor.create(1);
    private final Flux<AgencyState> statesStream = statesSink.publishOn(Schedulers.elastic());

    private final Map<MissionHandle, MissionExecutor> executors = new ConcurrentHashMap<>();
    private final Set<MissionInstance> instances = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final MissionHandleFactory handleFactory = new AtomicIncrementMissionHandleFactory(this);
    private final ExecutorService moleExecutor = newSingleThreadExecutor(ThreadFactories.namedThreadFactory("java-mole-%d"));

    private final Set<Mission> availableMissions;

    protected AbstractJavaMole(Set<Mission> availableMissions) {
        this.availableMissions = availableMissions;
        publishState();
    }

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        return supplyAsync(() -> {
            MissionHandle handle = handleFactory.createHandle();
            executors.put(handle, executorFor(handle, mission, params));
            instances.add(new MissionInstance(handle, mission));
            publishState();
            return handle;
        });
    }

    @Override
    public Mono<MissionParameterDescription> parameterDescriptionOf(Mission mission) {
        return supplyAsync(() -> missionParameterDescriptionOf(mission));
    }

    @Override
    public Flux<AgencyState> states() {
        return this.statesStream;
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

    @Override
    public final Flux<MissionLog> logsFor(MissionHandle handle) {
        LogConsumer consumers = getConsumer(RunningMissionLogConsumer.class);
        if (consumers == null) {
            return Flux.error(new IllegalStateException("Could not get consumer for '" + RunningMissionLogConsumer.class + "'"));
        }
        return fromConsumerOrError(consumers, handle);
    }

    private LogConsumer getConsumer(Class<?> clz) {
        try {
            return LogConsumerFactory.consumer(clz);
        } catch (Exception e) {
            return null;
        }
    }

    private <T> Flux<T> fromConsumerOrError(LogConsumer consumer, MissionHandle handle) {
        return Optional.ofNullable(consumer.asStream(handle))
                .orElse(Flux.error(new IllegalStateException("No log producer for handle '" + handle + "'")));
    }


    @Override
    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return supplyAsync(() -> missionRepresentationOf(mission));
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

    private void publishState() {
        statesSink.onNext(ImmutableAgencyState.of(ImmutableSet.copyOf(availableMissions), ImmutableList.copyOf(instances)));
    }

    protected abstract MissionExecutor executorFor(MissionHandle handle, Mission mission, Map<String, Object> params);

    protected abstract MissionRepresentation missionRepresentationOf(Mission mission);

    protected abstract MissionParameterDescription missionParameterDescriptionOf(Mission mission);
}
