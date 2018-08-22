/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.server.local;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.concurrent.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import com.google.common.collect.ImmutableSet;
import org.molr.commons.api.service.Agency;
import org.molr.mole.api.Supervisor;
import org.molr.commons.api.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * This is probably the most simple agency inventible: It is employing several moles, instantiating a mission on the
 * first one who can do it
 *
 * @author kfuchsbe
 */
public class LocalMoleDelegationAgency implements Agency {

    private final Map<Mission, Supervisor> missionMoles;
    private final ConcurrentMap<MissionHandle, Supervisor> activeMoles = new ConcurrentHashMap<>();
    private final ConcurrentMap<MissionHandle, MissionInstance> missionInstances = new ConcurrentHashMap<>();
    private final MissionHandleFactory missionHandleFactory;
    private final ReplayProcessor<AgencyState> states = ReplayProcessor.create(1);
    private final ReplayProcessor<Set<Mission>> missions = ReplayProcessor.create(1);


    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Scheduler scheduler = Schedulers.elastic();

    public LocalMoleDelegationAgency(MissionHandleFactory missionHandleFactory, Iterable<Supervisor> moles) {
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
            Supervisor supervisor = missionMoles.get(mission);
            supervisor.instantiate(handle, mission, params);
            activeMoles.put(handle, supervisor);
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
        Supervisor activeSupervisor = activeMoles.get(handle);
        if (activeSupervisor == null) {
            return Flux.error(new IllegalStateException("No active mole for mission handle '" + handle + "' found. Probably no mission was instantiated with this id?"));
        }
        System.out.println("Publishing states from supervisor " + activeSupervisor);
        return activeSupervisor.statesFor(handle);
    }

    @Override
    public void instruct(MissionHandle handle, MissionCommand command) {
        activeMoles.get(handle).instruct(handle, command);
    }

    private static final Map<Mission, Supervisor> scanMoles(Iterable<Supervisor> moles) {
        Builder<Mission, Supervisor> builder = ImmutableMap.builder();
        for (Supervisor supervisor : moles) {
            for (Mission mission : supervisor.availableMissions()) {
                builder.put(mission, supervisor);
            }
        }
        return builder.build();
    }

}
