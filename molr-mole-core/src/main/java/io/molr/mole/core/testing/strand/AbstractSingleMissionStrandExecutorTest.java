package io.molr.mole.core.testing.strand;

import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.MissionInput;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.mole.core.runnable.RunStates;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import io.molr.mole.core.testing.LatchTestSupport;
import io.molr.mole.core.tree.*;
import io.molr.mole.core.tree.tracking.TreeTracker;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.Assertions;
import org.junit.Before;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Abstract support class for a test against one mission, specified via the {@link #mission()} abstract method.
 */
public abstract class AbstractSingleMissionStrandExecutorTest implements SingleMissionStrandExecutorTestSupport,
        MissionCreationTestSupport, LatchTestSupport {

    private TreeStructure treeStructure;
    private TreeTracker<Result> resultTracker;
    private LeafExecutor leafExecutor;
    private StrandExecutorFactory strandExecutorFactory;
    private StrandExecutor strandExecutor;

    protected abstract RunnableLeafsMission mission();
    
    @Before
    public void setUpAbstract() {
        setUpAbstract(ExecutionStrategy.PAUSE_ON_ERROR);
    }
    
    public void setUpAbstract(ExecutionStrategy executionStrategy) {
        RunnableLeafsMission mission = mission();

        treeStructure = mission.treeStructure();
        resultTracker = TreeTracker.create(treeStructure.missionRepresentation(), Result.UNDEFINED, Result::summaryOf);
        TreeTracker<RunState> runStateTracker = TreeTracker.create(treeStructure.missionRepresentation(), RunState.NOT_STARTED, RunState::summaryOf);

		leafExecutor = new RunnableBlockExecutor(resultTracker, mission.runnables(), MissionInput.empty(),
				new HashMap<>(), new ConcurrentMissionOutputCollector(), runStateTracker);
        strandExecutorFactory = new StrandExecutorFactory(leafExecutor, new RunStates(treeStructure));
        strandExecutor = strandExecutorFactory.createRootStrandExecutor(treeStructure, new HashSet<>(), new HashSet<>(), executionStrategy);
    }

    @Override
    public StrandExecutor rootStrandExecutor() {
        return strandExecutor;
    }

    @Override
    public TreeTracker<Result> treeResultTracker() {
        return resultTracker;
    }

    protected TreeStructure treeStructure() {
        return treeStructure;
    }

    protected LeafExecutor leafExecutor() {
        return leafExecutor;
    }

    protected StrandExecutorFactory strandExecutorFactory() {
        return strandExecutorFactory;
    }

    public AbstractComparableAssert<?, Result> assertThatRootResult() {
        return Assertions.assertThat(treeResultTracker().resultFor(treeStructure().rootBlock()));
    }
}
