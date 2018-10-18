/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.agency.server.local;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
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
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Objects.requireNonNull;

/**
 * This is probably the most simple agency inventible: It is employing several moles, instantiating a mission on the
 * first one who can do it
 *
 * @author kfuchsbe
 */
public class LocalMoleDelegationAgency implements Agency {

    private final Map<Mission, Mole> missionMoles;
    private final ConcurrentMap<MissionHandle, Mole> activeMoles = new ConcurrentHashMap<>();
    private final ConcurrentMap<MissionHandle, MissionInstance> missionInstances = new ConcurrentHashMap<>();
    private final MissionHandleFactory missionHandleFactory;
    private final ReplayProcessor<AgencyState> states = ReplayProcessor.create(1);
    private final ReplayProcessor<Set<Mission>> missions = ReplayProcessor.create(1);


    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Scheduler scheduler = Schedulers.elastic();

    public LocalMoleDelegationAgency(MissionHandleFactory missionHandleFactory, Iterable<Mole> moles) {
        this.missionHandleFactory = requireNonNull(missionHandleFactory, "missionHandleFactory must not be null");
        requireNonNull(moles, "moles must not be null");
        this.missionMoles = scanMoles(moles);
        publishState();
    }

    @Override
    public Flux<AgencyState> states() {
        return states;
    }

    @Override
    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return missionMoles.get(mission).representationOf(mission);
    }

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        CompletableFuture<MissionHandle> future = CompletableFuture.supplyAsync(() -> {
            MissionHandle handle = missionHandleFactory.next();
            Mole mole = missionMoles.get(mission);
            mole.instantiate(handle, mission, params);
            activeMoles.put(handle, mole);
            MissionInstance instance = new MissionInstance(handle, mission);
            missionInstances.put(handle, instance);
            return handle;
        }, executorService);

        return Mono.fromFuture(future).doOnNext(mh -> this.publishState()).cache();
    }

    private final void publishState() {
        states.onNext(ImmutableAgencyState.of(ImmutableSet.copyOf(missionMoles.keySet()), missionInstances.values()));
    }


    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        Mole activeMole = activeMoles.get(handle);
        if (activeMole == null) {
            return Flux.error(new IllegalStateException("No active mole for mission handle '" + handle + "' found. Probably no mission was instantiated with this id?"));
        }
        System.out.println("Publishing states from supervisor " + activeMole);
        return activeMole.statesFor(handle);
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        activeMoles.get(handle).instruct(handle, strand, command);
    }

    private static final Map<Mission, Mole> scanMoles(Iterable<Mole> moles) {
        Builder<Mission, Mole> builder = ImmutableMap.builder();
        for (Mole mole : moles) {
            for (Mission mission : mole.availableMissions()) {
                builder.put(mission, mole);
            }
        }
        return builder.build();
    }

}
