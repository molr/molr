/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.agency.server.local;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import org.molr.agency.core.Agency;
import org.molr.commons.domain.*;
import org.molr.mole.core.api.Mole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.molr.commons.util.Exceptions.illegalArgumentException;
import static org.molr.mole.core.utils.ThreadFactories.namedThreadFactory;

/**
 * This is probably the most simple agency possible: it is employing several moles, instantiating a mission on the first
 * one who can do it
 * <p>
 * This agency is threadsafe by delegating all the methods execution to a separate thread. All the methods that return
 * {@link Flux} or {@link Mono} are asynchronous.
 *
 * @author kfuchsbe
 */
public class LocalMoleDelegationAgency implements Agency {

    private final Map<Mission, Mole> missionMoles;

    /* TODO REMOVE? */
    private final ConcurrentMap<MissionHandle, MissionInstance> missionInstances = new ConcurrentHashMap<>();
    private final ConcurrentMap<MissionHandle, Mole> activeMoles = new ConcurrentHashMap<>();


    private final ReplayProcessor<AgencyState> statesSink = ReplayProcessor.create(1);
    private final Flux<AgencyState> statesStream = statesSink.publishOn(Schedulers.elastic());
    private final ExecutorService agencyExecutor = newSingleThreadExecutor(namedThreadFactory("local-agency-%d"));

    public LocalMoleDelegationAgency(Iterable<Mole> moles) {
        requireNonNull(moles, "moles must not be null");
        this.missionMoles = scanMolesForMissions(moles);
        publishState();
    }

    @Override
    public Flux<AgencyState> states() {
        return statesStream;
    }

    @Override
    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return supplyOnAgencyExecutorAsync(() -> missionMoles.get(mission).representationOf(mission));
    }

    @Override
    public Mono<MissionParameterDescription> parameterDescriptionOf(Mission mission) {
        return supplyOnAgencyExecutorAsync(() -> missionMoles.get(mission).parameterDescriptionOf(mission));
    }

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        return supplyOnAgencyExecutorAsync(() -> {
            Mole mole = missionMoles.get(mission);
            if (mole == null) {
                throw new IllegalArgumentException("No mole could be found for mission '" + mission + "'.");
            }
            return mole;
        }).flatMap(mole -> mole.instantiate(mission, params)
                .doOnNext(missionHandle -> activeMoles.put(missionHandle, mole))
                .doOnNext(missionHandle -> missionInstances.put(missionHandle, new MissionInstance(missionHandle, mission))))
                .doOnNext(mh -> this.publishState()).cache();
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return fromActiveMoleOrError(handle, m -> m.statesFor(handle));
    }

    @Override
    public Flux<MissionOutput> outputsFor(MissionHandle handle) {
        return fromActiveMoleOrError(handle, m -> m.outputsFor(handle));
    }

    @Override
    public Flux<MissionRepresentation> representationsFor(MissionHandle handle) {
        return fromActiveMoleOrError(handle, m -> m.representationsFor(handle));
    }

    private <T> Flux<T> fromActiveMoleOrError(MissionHandle handle, Function<Mole, Flux<T>> fluxMapper) {
        return supplyOnAgencyExecutorSync(() -> {
            try {
                return fluxMapper.apply(getMoleWithId(handle));
            } catch (Exception ex) {
                return Flux.error(new IllegalStateException("No active mole for mission handle '" + handle + "' found. Probably no mission was instantiated with this id?", ex));
            }
        });
    }

    private Mole getMoleWithId(MissionHandle moleHandle) {
        return ofNullable(activeMoles.get(moleHandle)).orElseThrow(() -> illegalArgumentException("Cannot find mole with handle {}", moleHandle));
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        runOnAgencyExecutorSync(() -> getMoleWithId(handle).instruct(handle, strand, command));
    }

    @Override
    public void instructRoot(MissionHandle handle, StrandCommand command) {
        runOnAgencyExecutorSync(() -> getMoleWithId(handle).instructRoot(handle, command));
    }

    private void publishState() {
        statesSink.onNext(ImmutableAgencyState.of(ImmutableSet.copyOf(missionMoles.keySet()), ImmutableList.copyOf(missionInstances.values())));
    }

    private static Map<Mission, Mole> scanMolesForMissions(Iterable<Mole> moles) {
        Builder<Mission, Mole> builder = ImmutableMap.builder();
        for (Mole mole : moles) {
            for (Mission mission : mole.availableMissions()) {
                builder.put(mission, mole);
            }
        }
        return builder.build();
    }

    private void runOnAgencyExecutorSync(Runnable runnable) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(runnable, agencyExecutor);
        try {
            future.get();
        } catch (Exception e) {
            throw new RuntimeException("Exception while running on agency executor", e);
        }
    }

    private <T> T supplyOnAgencyExecutorSync(Supplier<T> supplier) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier, agencyExecutor);
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException("Exception while running supplier on agency executor", e);
        }
    }

    private <T> Mono<T> supplyOnAgencyExecutorAsync(Supplier<T> supplier) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier, agencyExecutor);
        return Mono.fromFuture(future);
    }
}
