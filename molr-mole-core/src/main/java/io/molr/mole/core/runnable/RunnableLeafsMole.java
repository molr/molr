package io.molr.mole.core.runnable;

import io.molr.commons.domain.*;
import io.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import io.molr.mole.core.tree.*;
import io.molr.mole.core.tree.tracking.TreeTracker;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class RunnableLeafsMole extends AbstractJavaMole {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(RunnableLeafsMole.class);

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

        ExecutionStrategy executionStrategy = inferExecutionStrategyFromParameters(missionParameterDescriptionOf(mission), input);
        LOGGER.info("ExecutionStrategy: "+executionStrategy);
        LeafExecutor leafExecutor = new RunnableBlockExecutor(resultTracker, runnableLeafMission.runnables(), input, outputCollector, runStateTracker);
        return new TreeMissionExecutor(treeStructure, leafExecutor, resultTracker, outputCollector, runStateTracker, executionStrategy);
    }
    
    private static MissionInput missionInput(RunnableLeafsMission mission, Map<String, Object> params) {
        MissionInput in = MissionInput.from(params);

        Function<In, ?> contextFactory = mission.contextFactory();
        if (contextFactory == null) {
            return in;
        }
        return in.and(Placeholders.context().name(), contextFactory.apply(in));
    }
    
    private static ExecutionStrategy inferExecutionStrategyFromParameters(MissionParameterDescription parameterDescription, MissionInput input) {
        ExecutionStrategy executionStrategy = ExecutionStrategy.PAUSE_ON_ERROR;
        if(parameterDescription.hasParameterForPlaceholder(Placeholders.EXECUTION_STRATEGY)) {
            if(input.get(Placeholders.EXECUTION_STRATEGY) != null) {
                String executionStrategyString = input.get(Placeholders.EXECUTION_STRATEGY);
                executionStrategy = ExecutionStrategy.forName(executionStrategyString);
            }
        }
        else {
            LOGGER.warn("Selected ExecutionStrategy has been ignored since corresponding parameter is not specified in parameter description");
        }

        /*
         * Exception and/or log entry if lenient mode parameter is provided by params but not defined in mission definition
         */
        return executionStrategy;
    }

}
