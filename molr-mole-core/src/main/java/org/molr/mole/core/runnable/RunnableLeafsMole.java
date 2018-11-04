package org.molr.mole.core.runnable;

import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionRepresentation;
import org.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import org.molr.mole.core.tree.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;

public class RunnableLeafsMole extends AbstractJavaMole {

    private final Map<Mission, RunnableLeafsMission> missions;

    public RunnableLeafsMole(Set<RunnableLeafsMission> missions) {
        this.missions = createMissionsMap(requireNonNull(missions, "missions must not be null"));
    }

    private Map<Mission, RunnableLeafsMission> createMissionsMap(Set<RunnableLeafsMission> newMissions) {
        return newMissions.stream()
                .collect(toImmutableMap(m -> new Mission(m.name()), identity()));
    }

    @Override
    public Set<Mission> availableMissions() {
        return missions.keySet();
    }

    @Override
    public MissionRepresentation representationOf(Mission mission) {
        RunnableLeafsMission runnableMission = missions.get(mission);
        if(runnableMission == null) {
            throw new IllegalArgumentException(mission + " is not a mission of this mole");
        }
        return runnableMission.treeStructure().missionRepresentation();
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
