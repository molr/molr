/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.server.local;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import org.molr.server.api.Agency;
import org.molr.mole.api.Mole;
import org.molr.commons.api.domain.*;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * This is probably the most simple agency inventible: It is employing several moles, instantiating a mission on the
 * first one who can do it
 *
 * @author kfuchsbe
 */
public class LocalDelegationAgency implements Agency {

    private final Map<Mission, Mole> missionMoles;
    private final ConcurrentMap<MissionHandle, Mole> activeMoles = new ConcurrentHashMap<>();
    private final ConcurrentMap<MissionHandle, MissionInstance> missionInstances = new ConcurrentHashMap<>();
    private final MissionHandleFactory missionHandleFactory;
    private final EmitterProcessor<AgencyState> states = EmitterProcessor.create(1);

    private final Scheduler scheduler = Schedulers.elastic();

    public LocalDelegationAgency(MissionHandleFactory missionHandleFactory, Iterable<Mole> moles) {
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
    public Flux<Mission> executableMissions() {
        return Flux.fromIterable(missionMoles.keySet()).sort(comparing(Mission::name));
    }

    @Override
    public Mono<MissionDescription> representationOf(Mission mission) {
        return missionMoles.get(mission).representationOf(mission);
    }

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        Mono<MissionHandle> mono = Mono.fromSupplier(() -> {
            MissionHandle handle = missionHandleFactory.next();
            Mole mole = missionMoles.get(mission);
            mole.instantiate(handle, mission, params);
            activeMoles.put(handle, mole);
            MissionInstance instance = new MissionInstance(handle, mission);
            missionInstances.put(handle, instance);
            return handle;
        }).doOnNext(mh -> this.publishState()).cache();
        mono.subscribeOn(scheduler).subscribe();
        return mono;
    }

    private final void publishState() {
        states.onNext(ImmutableAgencyState.of(missionInstances.values()));
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return activeMoles.get(handle).statesFor(handle);
    }

    @Override
    public void instruct(MissionHandle handle, MissionCommand command) {
        activeMoles.get(handle).instruct(handle, command);
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
