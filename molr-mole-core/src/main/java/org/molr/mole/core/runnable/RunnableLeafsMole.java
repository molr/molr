package org.molr.mole.core.runnable;

import com.google.common.collect.ImmutableMap;
import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionRepresentation;
import org.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import org.molr.mole.core.tree.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;

public class RunnableLeafsMole extends AbstractJavaMole {

    private final Map<Mission, RunnableLeafsMission> missions;

    public RunnableLeafsMole(Set<RunnableLeafsMission> missions) {
        this.missions = createMap(requireNonNull(missions, "missions must not be null"));
    }

    private Map<Mission, RunnableLeafsMission> createMap(Set<RunnableLeafsMission> newMissions) {
        return newMissions.stream()
                .collect(toImmutableMap(m -> new Mission(m.name()), identity()));
    }

    @Override
    public Set<Mission> availableMissions() {
        return missions.keySet();
    }

    @Override
    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return Mono.just(missions.get(mission).treeStructure().missionRepresentation());
    }


    @Override
    protected MissionExecutor instantiate(Mission mission, Map<String, Object> params) {
        RunnableLeafsMission runnableLeafMission = missions.get(mission);
        TreeStructure treeStructure = runnableLeafMission.treeStructure();
        TreeResultTracker resultTracker = new TreeResultTracker(treeStructure.missionRepresentation());
        LeafExecutor leafExecutor = new RunnableBlockExecutor(resultTracker, runnableLeafMission.runnables());
        return new TreeMissionExecutor(treeStructure, leafExecutor, resultTracker);
    }
}
