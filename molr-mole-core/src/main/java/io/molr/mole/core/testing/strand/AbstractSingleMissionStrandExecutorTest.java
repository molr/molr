package io.molr.mole.core.testing.strand;

import java.util.HashMap;
import java.util.HashSet;

import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.Assertions;
import org.junit.Before;

import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.MissionInput;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.testing.LatchTestSupport;
import io.molr.mole.core.tree.ConcurrentMissionOutputCollector;
import io.molr.mole.core.tree.LeafExecutor;
import io.molr.mole.core.tree.StateTrackingBlockExecutor;
import io.molr.mole.core.tree.StrandExecutor;
import io.molr.mole.core.tree.TreeNodeStates;
import io.molr.mole.core.tree.TreeStructure;
import io.molr.mole.core.tree.executor.StrandExecutorFactory;
import io.molr.mole.core.tree.tracking.TreeTracker;

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
        setUpAbstract(executionStrategy, mission());
    }
    
    public void setUpAbstract(ExecutionStrategy executionStrategy, RunnableLeafsMission mission) {

        treeStructure = mission.treeStructure();
        resultTracker = TreeTracker.create(treeStructure.missionRepresentation(), Result.UNDEFINED, Result::summaryOf);
        TreeTracker<RunState> runStateTracker = TreeTracker.create(treeStructure.missionRepresentation(), RunState.NOT_STARTED, RunState::summaryOf);

		leafExecutor = new StateTrackingBlockExecutor(resultTracker, mission.runnables(), MissionInput.empty(),
				new HashMap<>(), new ConcurrentMissionOutputCollector(), runStateTracker);
        strandExecutorFactory = new StrandExecutorFactory(leafExecutor, new TreeNodeStates(treeStructure));
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

//    protected StrandExecutorFactory strandExecutorFactory() {
//        return strandExecutorFactory;
//    }

    public AbstractComparableAssert<?, Result> assertThatRootResult() {
        return Assertions.assertThat(treeResultTracker().resultFor(treeStructure().rootBlock()));
    }
}
