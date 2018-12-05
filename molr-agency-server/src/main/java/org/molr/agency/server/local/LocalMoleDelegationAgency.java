/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.agency.server.local;

import org.molr.commons.api.Mole;
import org.molr.commons.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
public class LocalMoleDelegationAgency implements Mole {

    private final Map<Mission, Mole> missionMoles = new ConcurrentHashMap<>();

    /* TODO REMOVE? */
    private final Map<MissionHandle, Mole> activeMoles = new ConcurrentHashMap<>();

    private final Flux<AgencyState> statesStream;
    private final ExecutorService agencyExecutor = newSingleThreadExecutor(namedThreadFactory("local-agency-%d"));
    private final Scheduler agencyScheduler = Schedulers.fromExecutor(agencyExecutor);

    private final Scheduler stateScheduler = Schedulers.fromExecutor(newSingleThreadExecutor(namedThreadFactory("delegation-states-%d")));

    public LocalMoleDelegationAgency(Iterable<Mole> moles) {
        requireNonNull(moles, "moles must not be null");
        Set<Flux<AgencyState>> stateStreams = StreamSupport.stream(moles.spliterator(), false).map(m -> m.states()).collect(Collectors.toSet());

        for (Mole mole : moles) {
            mole.states().publishOn(agencyScheduler).subscribe(state -> {
                Set<Mission> updatedMissions = state.executableMissions();

                for (Mission mission : updatedMissions) {
                    missionMoles.putIfAbsent(mission, mole);
                }

                missionMoles.entrySet().stream()
                        .filter(e -> e.getValue().equals(mole))
                        .map(Map.Entry::getKey)
                        .filter(m -> !updatedMissions.contains(m))
                        .forEach(missionMoles::remove);
            });
        }

        this.statesStream = Flux.combineLatest(stateStreams, streams -> ImmutableAgencyState.combine(streams)).publishOn(stateScheduler);
    }

    @Override
    public Flux<AgencyState> states() {
        return statesStream;
    }

    @Override
    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return getMole(mission).flatMap(mole -> mole.representationOf(mission));
    }

    @Override
    public Mono<MissionParameterDescription> parameterDescriptionOf(Mission mission) {
        return getMole(mission).flatMap(mole -> mole.parameterDescriptionOf(mission));
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
                .doOnNext(missionHandle -> activeMoles.put(missionHandle, mole)))
                .cache();
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

    private Mono<Mole> getMole(Mission mission) {
        Mole mole = missionMoles.get(mission);
        if (mole == null) {
            return Mono.error(illegalArgumentException("{} is not handled by any mole", mission));
        }
        return Mono.just(mole);
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
