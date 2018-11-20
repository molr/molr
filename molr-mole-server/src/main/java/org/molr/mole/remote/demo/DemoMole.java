package org.molr.mole.remote.demo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.molr.commons.domain.*;
import org.molr.mole.core.api.Mole;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DemoMole implements Mole {
    private final AtomicLong ids = new AtomicLong(0);

    private final Set<Mission> dummyMissions = ImmutableSet.of(new Mission("Find Dr No."), new Mission("Conquer Rome"));

    private final Map<Mission, MissionRepresentation> missions =
            ImmutableMap.of(new Mission("Find Dr No."), dummyTree("Find Dr No."),
                    new Mission("Conquer Rome"), dummyTree("Conquer Rome"),
                    new Mission("Linear Mission"), linear("Linear Mission"));

    private final Map<MissionHandle, MissionRepresentation> instances = new ConcurrentHashMap<>();

    private MissionRepresentation linear(String missionName) {
        Block rootNode = Block.builder(id(), missionName).build();

        ImmutableMissionRepresentation.Builder builder = ImmutableMissionRepresentation.builder(rootNode);
        for (int i = 0; i < 10; i++) {
            Block child = Block.idAndText(id(), "Block no " + i + "");
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


    private MissionRepresentation dummyTree(String rootName) {
        Block root = Block.builder(id(), rootName).build();
        ImmutableMissionRepresentation.Builder builder = ImmutableMissionRepresentation.builder(root);

        Block ss1 = Block.idAndText(id(), "subSeq 1");
        builder.parentToChild(root, ss1);

        builder.parentToChild(ss1, Block.idAndText(id(), "Leaf 1A"));
        builder.parentToChild(ss1, Block.idAndText(id(), "Leaf 1B"));

        Block ss2 = Block.idAndText(id(), "subSeq 2");
        builder.parentToChild(root, ss2);
        builder.parentToChild(ss2, Block.idAndText(id(), "Leaf 2A"));
        builder.parentToChild(ss2, Block.idAndText(id(), "Leaf 2B"));

        return builder.build();
    }

    private String id() {
        return "" + ids.getAndIncrement();
    }
}

