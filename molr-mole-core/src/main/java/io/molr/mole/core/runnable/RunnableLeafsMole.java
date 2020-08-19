package io.molr.mole.core.runnable;

import io.molr.commons.domain.*;
import io.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import io.molr.mole.core.tree.*;
import io.molr.mole.core.tree.tracking.TreeTracker;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class RunnableLeafsMole extends AbstractJavaMole {

    private final Map<Mission, RunnableLeafsMission> missions;

    public RunnableLeafsMole(Set<RunnableLeafsMission> missions) {
        super(extractMissions(missions));
        this.missions = createMissionsMap(missions);
    }

    private static Set<Mission> extractMissions(Set<RunnableLeafsMission> missions) {
        requireNonNull(missions, "missions must not be null");
        return missions.stream().map(rlm -> new Mission(rlm.name())).collect(toSet());
    }

    private static Map<Mission, RunnableLeafsMission> createMissionsMap(Set<RunnableLeafsMission> newMissions) {
        return newMissions.stream()
                .collect(toMap(m -> new Mission(m.name()), identity()));
    }

    @Override
    public MissionRepresentation missionRepresentationOf(Mission mission) {
        return getOrThrow(mission).treeStructure().missionRepresentation();
    }

    @Override
    public MissionParameterDescription missionParameterDescriptionOf(Mission mission) {
        return getOrThrow(mission).parameterDescription();
    }


    private RunnableLeafsMission getOrThrow(Mission mission) {
        RunnableLeafsMission runnableMission = missions.get(mission);
        if (runnableMission == null) {
            throw new IllegalArgumentException(mission + " is not a mission of this mole");
        }
        return runnableMission;
    }


    @Override
    protected MissionExecutor executorFor(Mission mission, Map<String, Object> params) {
        RunnableLeafsMission runnableLeafMission = missions.get(mission);
        TreeStructure treeStructure = runnableLeafMission.treeStructure();
        TreeTracker<Result> resultTracker = TreeTracker.create(treeStructure.missionRepresentation(), Result.UNDEFINED, Result::summaryOf);
        TreeTracker<RunState> runStateTracker = TreeTracker.create(treeStructure.missionRepresentation(), RunState.UNDEFINED, RunState::summaryOf);

        MissionOutputCollector outputCollector = new ConcurrentMissionOutputCollector();
        MissionInput input = missionInput(runnableLeafMission, params);

        boolean lenientMode = inferLenientModeFromParameters(missionParameterDescriptionOf(mission), input);
        LeafExecutor leafExecutor = new RunnableBlockExecutor(resultTracker, runnableLeafMission.runnables(), input, outputCollector, runStateTracker);
        return new TreeMissionExecutor(treeStructure, leafExecutor, resultTracker, outputCollector, runStateTracker, lenientMode);
    }
    
    private static MissionInput missionInput(RunnableLeafsMission mission, Map<String, Object> params) {
        MissionInput in = MissionInput.from(params);

        Function<In, ?> contextFactory = mission.contextFactory();
        if (contextFactory == null) {
            return in;
        }
        return in.and(Placeholders.context().name(), contextFactory.apply(in));
    }
    
    private static boolean inferLenientModeFromParameters(MissionParameterDescription parameterDescription, MissionInput input) {
        boolean lenientMode = false;
        if(parameterDescription.hasParameterForPlaceholder(Placeholders.LENIENT_MODE)) {
            if(input.get(Placeholders.LENIENT_MODE) != null) {
                lenientMode = input.get(Placeholders.LENIENT_MODE).booleanValue();
            }
        }
        /*
         * Exception and/or log entry if lenient mode parameter is provided by params but not defined in mission definition
         */
        return lenientMode;
    }

}
