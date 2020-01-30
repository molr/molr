package io.molr.mole.core.runnable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionInput;
import io.molr.commons.domain.MissionParameterDescription;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import io.molr.mole.core.tree.AbstractJavaMole;
import io.molr.mole.core.tree.ConcurrentMissionOutputCollector;
import io.molr.mole.core.tree.LeafExecutor;
import io.molr.mole.core.tree.MissionExecutor;
import io.molr.mole.core.tree.MissionOutputCollector;
import io.molr.mole.core.tree.TreeMissionExecutor;
import io.molr.mole.core.tree.TreeStructure;
import io.molr.mole.core.tree.tracking.TreeTracker;

/**
 * This is the general form of a runnable leafs mole. Still, the idea is the same: The mission is represented by a tree
 * structure and the leafs are executables. However, this is more general as the RunnableLeafsMole: Not everything has
 * to be defined at instantiation time of the mole. Instead, the required information is provided through callback
 * functions.
 * <p>
 * Important note: Currently, a change of the TreeStructure is not yet supported! So, currently, there are no checks if
 * different tree structures are provided through subsequent calls of the treeSTructureCallback. They MUST be always the
 * same. Otherwise behavior is not guaranteed. The same applies for the parameterDescription.
 * <p>
 * However (and this is the main purpose of the exercise), different runnables can be generated for different
 * parameters. Therefore, it is the runnablesCallback is called only once, at instantiation time of the sequence.
 */
public abstract class RunnableLeafsMoleBase extends AbstractJavaMole {

    private final BiFunction<Mission, Map<String, Object>, Map<Block, BiConsumer<In, Out>>> runnablesCallback;
    private final Function<Mission, TreeStructure> treeStructureCallback;
    private final Function<Mission, MissionParameterDescription> parameterDescriptionCallback;

    public RunnableLeafsMoleBase(Set<Mission> availableMissions,
            BiFunction<Mission, Map<String, Object>, Map<Block, BiConsumer<In, Out>>> runnablesCallback,
            Function<Mission, TreeStructure> treeStructureCallback,
            Function<Mission, MissionParameterDescription> parameterDescriptionCallback) {
        super(availableMissions);
        this.runnablesCallback = runnablesCallback;
        this.treeStructureCallback = treeStructureCallback;
        this.parameterDescriptionCallback = parameterDescriptionCallback;
    }

    @Override
    public MissionRepresentation missionRepresentationOf(Mission mission) {
        return treeStructure(mission).missionRepresentation();
    }

    @Override
    public MissionParameterDescription missionParameterDescriptionOf(Mission mission) {
        return parameterDescription(mission);
    }

    @Override
    protected MissionExecutor executorFor(Mission mission, Map<String, Object> params) {
        TreeStructure treeStructure = treeStructure(mission);
        TreeTracker<Result> resultTracker = TreeTracker.create(treeStructure.missionRepresentation(), Result.UNDEFINED,
                Result::summaryOf);
        TreeTracker<RunState> runStateTracker = TreeTracker.create(treeStructure.missionRepresentation(),
                RunState.UNDEFINED, RunState::summaryOf);

        MissionOutputCollector outputCollector = new ConcurrentMissionOutputCollector();

        LeafExecutor leafExecutor = new RunnableBlockExecutor(resultTracker, runnables(mission, params),
                MissionInput.from(params), outputCollector, runStateTracker);
        return new TreeMissionExecutor(treeStructure, leafExecutor, resultTracker, outputCollector, runStateTracker);
    }

    private TreeStructure treeStructure(Mission mission) {
        return getOrThrow(mission, treeStructureCallback);
    }

    private MissionParameterDescription parameterDescription(Mission mission) {
        return getOrThrow(mission, parameterDescriptionCallback);
    }

    private Map<Block, BiConsumer<In, Out>> runnables(Mission mission, Map<String, Object> params) {
        return Optional.ofNullable(runnablesCallback.apply(mission, params)).orElseThrow(notFromHere(mission));
    }

    private static <T> T getOrThrow(Mission mission, Function<Mission, T> function) {
        return Optional.ofNullable(function.apply(mission)).orElseThrow(notFromHere(mission));
    }

    private static Supplier<? extends IllegalArgumentException> notFromHere(Mission mission) {
        return () -> new IllegalArgumentException(mission + " is not a mission of this mole");
    }

}
