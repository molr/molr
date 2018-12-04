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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
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
    private final MissionHandleFactory missionHandleFactory;


//    private final ConcurrentMap<MissionHandle, Mole> activeMoles = new ConcurrentHashMap<>();
    /* TODO REMOVE*/
    private final ConcurrentMap<MissionHandle, MissionInstance> missionInstances = new ConcurrentHashMap<>();


    private final ReplayProcessor<AgencyState> statesSink = ReplayProcessor.create(1);
    private final Flux<AgencyState> statesStream = statesSink.publishOn(Schedulers.elastic());
    private final ExecutorService agencyExecutor = newSingleThreadExecutor(namedThreadFactory("local-agency-%d"));

    public LocalMoleDelegationAgency(MissionHandleFactory missionHandleFactory, Iterable<Mole> moles) {
        this.missionHandleFactory = requireNonNull(missionHandleFactory, "missionHandleFactory must not be null");
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
        }).flatMap(mole -> mole.instantiate(mission, params).map(moleHandle -> prependHandle(mole, moleHandle))
                .doOnNext(missionHandle -> missionInstances.put(missionHandle, new MissionInstance(missionHandle, mission))))
                .doOnNext(mh -> this.publishState()).cache();
    }

    private static MissionHandle prependHandle(Mole mole, MissionHandle moleScopedMissionHandle) {
        MissionHandle moleHandle = moleHandleFor(mole);
        String moleScopedMissionId = moleScopedMissionHandle.id();
        return MissionHandle.ofId(format("%s::%s", moleHandle.id(), moleScopedMissionId));
    }

    private static MissionHandle moleHandleFor(Mole mole) {
        String moleClass = mole.getClass().getCanonicalName();
        String moleUid = mole.uid();
        return MissionHandle.ofId(format("%s[%s]", moleClass, moleUid));
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return fromActiveMoleOrError(handle, m -> m.statesFor(extractMissionHandle(handle)));
    }

    @Override
    public Flux<MissionOutput> outputsFor(MissionHandle handle) {
        return fromActiveMoleOrError(handle, m -> m.outputsFor(extractMissionHandle(handle)));
    }

    @Override
    public Flux<MissionRepresentation> representationsFor(MissionHandle handle) {
        return fromActiveMoleOrError(handle, m -> m.representationsFor(extractMissionHandle(handle)));
    }

    private static String[] splitHandle(MissionHandle fullHandle) {
        String[] split = fullHandle.id().split("::");
        if (split.length < 2) {
            throw illegalArgumentException("Could not split handle {} into 2 using ::", fullHandle);
        }
        return split;
    }

    private static MissionHandle extractMoleHandle(MissionHandle fullHandle) {
        return MissionHandle.ofId(splitHandle(fullHandle)[0]);
    }

    private static MissionHandle extractMissionHandle(MissionHandle fullHandle) {
        return MissionHandle.ofId(splitHandle(fullHandle)[1]);
    }

    private Mole getMoleWithId(MissionHandle moleHandle) {
        return this.missionMoles.values().stream()
                .filter(mole -> moleHandleFor(mole).equals(moleHandle))
                .findFirst().orElseThrow(() -> illegalArgumentException("Cannot find mole with handle {}", moleHandle));
    }

    private <T> Flux<T> fromActiveMoleOrError(MissionHandle handle, Function<Mole, Flux<T>> fluxMapper) {
        return supplyOnAgencyExecutorSync(() -> {
            try{
                return fluxMapper.apply(getMoleWithId(extractMoleHandle(handle)));
            } catch (Exception ex) {
                return Flux.error(new IllegalStateException("No active mole for mission handle '" + handle + "' found. Probably no mission was instantiated with this id?", ex));
            }
        });
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        runOnAgencyExecutorSync(() -> getMoleWithId(extractMoleHandle(handle)).instruct(extractMissionHandle(handle), strand, command));
    }

    @Override
    public void instructRoot(MissionHandle handle, StrandCommand command) {
        runOnAgencyExecutorSync(() -> getMoleWithId(extractMoleHandle(handle)).instructRoot(extractMissionHandle(handle), command));
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
