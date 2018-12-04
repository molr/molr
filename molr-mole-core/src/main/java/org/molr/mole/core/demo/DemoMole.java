package org.molr.mole.core.demo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.molr.commons.domain.*;
import org.molr.mole.core.api.Mole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DemoMole implements Mole {
    private final AtomicLong ids = new AtomicLong(0);

    private MissionHandleFactory handleFactory = new AtomicIncrementMissionHandleFactory();

    private final Set<Mission> dummyMissions = ImmutableSet.of(new Mission("Find Dr No."), new Mission("Conquer Rome"));

    private final Map<Mission, MissionRepresentation> missions =
            ImmutableMap.of(new Mission("Find Dr No."), dummyTree("Find Dr No."),
                    new Mission("Conquer Rome"), dummyTree("Conquer Rome"),
                    new Mission("Linear Mission"), linear("Linear Mission"));

    private final Map<MissionHandle, MissionRepresentation> instances = new ConcurrentHashMap<>();

    private MissionRepresentation linear(String missionName) {
        Block rootNode = Block.builder(blockId(), missionName).build();

        ImmutableMissionRepresentation.Builder builder = ImmutableMissionRepresentation.builder(rootNode);
        for (int i = 0; i < 10; i++) {
            Block child = Block.idAndText(blockId(), "Block no " + i + "");
            builder.parentToChild(rootNode, child);
        }
        return builder.build();
    }

    @Override
    public Flux<MissionRepresentation> representationsFor(MissionHandle handle) {
        return Flux.just(instances.get(handle));
    }


    @Override
    public Set<Mission> availableMissions() {
        return this.missions.keySet();
    }

    @Override
    public MissionRepresentation representationOf(Mission mission) {
        return missions.get(mission);
    }

    @Override
    public MissionParameterDescription parameterDescriptionOf(Mission mission) {
        return new MissionParameterDescription(Collections.emptySet());
    }

    @Override
    public void instantiate(MissionHandle handle, Mission mission, Map<String, Object> params) {
        instances.put(handle, missions.get(mission));
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return Flux.empty();
    }

    @Override
    public Flux<MissionOutput> outputsFor(MissionHandle handle) {
        return Flux.empty();
    }

    @Override
    public void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        /* NOOP for the moment */
    }

    @Override
    public void instructRoot(MissionHandle handle, StrandCommand command) {
        /* NOOP for the moment */
    }


    private MissionRepresentation dummyTree(String rootName) {
        Block root = Block.builder(blockId(), rootName).build();
        ImmutableMissionRepresentation.Builder builder = ImmutableMissionRepresentation.builder(root);

        Block ss1 = Block.idAndText(blockId(), "subSeq 1");
        builder.parentToChild(root, ss1);

        builder.parentToChild(ss1, Block.idAndText(blockId(), "Leaf 1A"));
        builder.parentToChild(ss1, Block.idAndText(blockId(), "Leaf 1B"));

        Block ss2 = Block.idAndText(blockId(), "subSeq 2");
        builder.parentToChild(root, ss2);
        builder.parentToChild(ss2, Block.idAndText(blockId(), "Leaf 2A"));
        builder.parentToChild(ss2, Block.idAndText(blockId(), "Leaf 2B"));

        return builder.build();
    }

    private String blockId() {
        return "" + ids.getAndIncrement();
    }

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        MissionHandle handle = handleFactory.createHandle();
        instantiate(handle, mission, params);
        return Mono.just(handle);
    }
}

