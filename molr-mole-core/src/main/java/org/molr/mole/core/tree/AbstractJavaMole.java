package org.molr.mole.core.tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.molr.commons.domain.AgencyState;
import org.molr.commons.domain.AtomicIncrementMissionHandleFactory;
import org.molr.commons.domain.ImmutableAgencyState;
import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.MissionHandleFactory;
import org.molr.commons.domain.MissionInstance;
import org.molr.commons.domain.MissionOutput;
import org.molr.commons.domain.MissionParameterDescription;
import org.molr.commons.domain.MissionRepresentation;
import org.molr.commons.domain.MissionState;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.api.Mole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.molr.mole.core.utils.ThreadFactories.namedThreadFactory;

public abstract class AbstractJavaMole implements Mole {

    private final ReplayProcessor<AgencyState> statesSink = ReplayProcessor.create(1);
    private final Flux<AgencyState> statesStream = statesSink.publishOn(Schedulers.elastic());

    private final Map<MissionHandle, MissionExecutor> executors = new ConcurrentHashMap<>();
    private final Set<MissionInstance> instances = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final MissionHandleFactory handleFactory = new AtomicIncrementMissionHandleFactory(this);
    private final ExecutorService moleExecutor = newSingleThreadExecutor(namedThreadFactory("java-mole-%d"));

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        return supplyAsync(() -> {
            MissionHandle handle = handleFactory.createHandle();
            executors.put(handle, executorFor(mission, params));
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

    @PostConstruct
    private void publishState() {
        statesSink.onNext(ImmutableAgencyState.of(ImmutableSet.copyOf(availableMissions()), ImmutableList.copyOf(instances)));
    }

    protected abstract MissionExecutor executorFor(Mission mission, Map<String, Object> params);

    protected abstract Set<Mission> availableMissions();

    protected abstract MissionRepresentation missionRepresentationOf(Mission mission);

    protected abstract MissionParameterDescription missionParameterDescriptionOf(Mission mission);
}
