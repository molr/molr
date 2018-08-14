package org.molr.server.demo;

//import cern.lhc.app.seq.scheduler.domain.execution.demo.SleepBlock;

import org.molr.mole.api.Mole;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.molr.commons.api.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class DemoMole implements Mole {

    private final AtomicLong ids = new AtomicLong(0);

    private final Set<Mission> dummyMissions = ImmutableSet.of(new Mission("Find Dr No."), new Mission("Conquer Rome"));

    private final Map<Mission, Block> missions =
            ImmutableMap.of(new Mission("Find Dr No."), dummyTree("Find Dr No."),
                    new Mission("Conquer Rome"), dummyTree("Conquer Rome"),
                    new Mission("Linear Mission"), linear("Linear Mission"));

    private Block linear(String missionName) {
        Block.Builder rootNodeBuilder = Block.builder(id(), missionName);
        for (int i = 0; i < 10; i++) {
            Block child = Block.idAndText(id(), "Block no " + i + "");
            rootNodeBuilder.child(child);
        }
        return rootNodeBuilder.build();
    }


    @Override
    public Set<Mission> availableMissions() {
        return this.missions.keySet();
    }

    @Override
    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return Mono.just(new ImmutableMissionRepresentation(mission, missions.get(mission)));
    }

    @Override
    public void instantiate(MissionHandle handle, Mission mission, Map<String, Object> params) {
        /* NOOP for the moment */
    }

    @Override
    public Flux<MissionState> statesFor(MissionHandle handle) {
        return Flux.empty();
    }

    @Override
    public void instruct(MissionHandle handle, MissionCommand command) {
        /* NOOP for the moment */
    }


    private Block dummyTree(String rootName) {
        Block l1a = Block.idAndText(id(), "Leaf 1A");
        Block l1b = Block.idAndText(id(), "Leaf 1B");
        Block ss1 = Block.builder(id(), "subSeq 1").child(l1a).child(l1b).build();
        Block ss2 = Block.builder(id(), "subSeq 2").child(Block.idAndText(id(), "Leaf 2A")).child(Block.idAndText(id(), "Leaf 2B"))
                .build();
        return Block.builder(id(), rootName).child(ss1).child(ss2).build();
    }

    private String id() {
        return "" + ids.getAndIncrement();
    }
}
