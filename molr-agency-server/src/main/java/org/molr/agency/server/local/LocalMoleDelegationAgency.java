/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.agency.server.local;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.molr.agency.core.Agency;
import org.molr.commons.domain.AgencyState;
import org.molr.commons.domain.ImmutableAgencyState;
import org.molr.commons.domain.Mission;
import org.molr.commons.domain.StrandCommand;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.MissionHandleFactory;
import org.molr.commons.domain.MissionInstance;
import org.molr.commons.domain.MissionRepresentation;
import org.molr.commons.domain.MissionState;
import org.molr.commons.domain.Strand;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.molr.mole.core.utils.ThreadFactories.namedThreadFactory;

/**
 * This is probably the most simple agency possible: it is employing several moles, instantiating a mission on the
 * first one who can do it
 * <p>
 * This agency is threadsafe by delegating all the methods execution to a separate thread. All the methods that return
 * {@link Flux} or {@link Mono} are asynchronous.
 *
 * @author kfuchsbe
 */
public class LocalMoleDelegationAgency implements Agency {

    private final Map<Mission, Mole> missionMoles;
    private final MissionHandleFactory missionHandleFactory;
    private final ConcurrentMap<MissionHandle, Mole> activeMoles = new ConcurrentHashMap<>();
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
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        return supplyOnAgencyExecutorAsync(() -> {
            MissionHandle handle = missionHandleFactory.createHandle();
            Mole mole = missionMoles.get(mission);
            mole.instantiate(handle, mission, params);
            activeMoles.put(handle, mole);
            MissionInstance instance = new MissionInstance(handle, mission);
            missionInstances.put(handle, instance);
            return handle;
        }).doOnNext(mh -> this.publishState()).cache();
    }


    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return supplyOnAgencyExecutorSync(() -> {
            Mole activeMole = activeMoles.get(handle);
            if (activeMole == null) {
                return Flux.error(new IllegalStateException("No active mole for mission handle '" + handle + "' found. Probably no mission was instantiated with this id?"));
            }
            return activeMole.statesFor(handle);
        });
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        runOnAgencyExecutorSync(() -> activeMoles.get(handle).instruct(handle, strand, command));
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
