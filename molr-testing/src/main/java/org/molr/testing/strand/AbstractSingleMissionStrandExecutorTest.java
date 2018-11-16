package org.molr.testing.strand;

import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.molr.commons.domain.MissionInput;
import org.molr.commons.domain.Result;
import org.molr.commons.domain.RunState;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import org.molr.mole.core.tree.ConcurrentMissionOutputCollector;
import org.molr.mole.core.tree.LeafExecutor;
import org.molr.mole.core.tree.StrandExecutor;
import org.molr.mole.core.tree.StrandExecutorFactory;
import org.molr.mole.core.tree.StrandFactory;
import org.molr.mole.core.tree.StrandFactoryImpl;
import org.molr.mole.core.tree.TreeStructure;
import org.molr.mole.core.tree.tracking.TreeTracker;
import org.molr.testing.LatchTestSupport;

/**
 * Abstract support class for a test against one mission, specified via the {@link #mission()} abstract method.
 */
public abstract class AbstractSingleMissionStrandExecutorTest implements SingleMissionStrandExecutorTestSupport,
        MissionCreationTestSupport, LatchTestSupport {

    private TreeStructure treeStructure;
    private TreeTracker<Result> resultTracker;
    private LeafExecutor leafExecutor;
    private StrandFactory strandFactory;
    private StrandExecutorFactory strandExecutorFactory;
    private StrandExecutor strandExecutor;

    protected abstract RunnableLeafsMission mission();

    @Before
    public void setUpAbstract() {
        RunnableLeafsMission mission = mission();

        treeStructure = mission.treeStructure();
        resultTracker = TreeTracker.create(treeStructure.missionRepresentation(), Result.UNDEFINED, Result::summaryOf);
        TreeTracker<RunState> runStateTracker = TreeTracker.create(treeStructure.missionRepresentation(), RunState.UNDEFINED, RunState::summaryOf);

        leafExecutor = new RunnableBlockExecutor(resultTracker, mission.runnables(), MissionInput.empty(), new ConcurrentMissionOutputCollector(), runStateTracker);
        strandFactory = new StrandFactoryImpl();
        strandExecutorFactory = new StrandExecutorFactory(strandFactory, leafExecutor);
        strandExecutor = strandExecutorFactory.createStrandExecutor(strandFactory.rootStrand(), treeStructure);
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

    protected StrandFactory strandFactory() {
        return strandFactory;
    }

    protected StrandExecutorFactory strandExecutorFactory() {
        return strandExecutorFactory;
    }

    public AbstractComparableAssert<?, Result> assertThatRootResult() {
        return Assertions.assertThat(treeResultTracker().resultFor(treeStructure().rootBlock()));
    }
}
