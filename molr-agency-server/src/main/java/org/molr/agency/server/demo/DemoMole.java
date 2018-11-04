package org.molr.agency.server.demo;

//import cern.lhc.app.seq.scheduler.domain.execution.demo.SleepBlock;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.ImmutableMissionRepresentation;
import org.molr.commons.domain.Mission;
import org.molr.commons.domain.StrandCommand;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.MissionRepresentation;
import org.molr.commons.domain.MissionState;
import org.molr.commons.domain.Strand;
import org.molr.mole.core.api.Mole;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class DemoMole implements Mole {

    private final AtomicLong ids = new AtomicLong(0);

    private final Set<Mission> dummyMissions = ImmutableSet.of(new Mission("Find Dr No."), new Mission("Conquer Rome"));

    private final Map<Mission, MissionRepresentation> missions =
            ImmutableMap.of(new Mission("Find Dr No."), dummyTree("Find Dr No."),
                    new Mission("Conquer Rome"), dummyTree("Conquer Rome"),
                    new Mission("Linear Mission"), linear("Linear Mission"));

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
    public Set<Mission> availableMissions() {
        return this.missions.keySet();
    }

    @Override
    public MissionRepresentation representationOf(Mission mission) {
        return missions.get(mission);
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
