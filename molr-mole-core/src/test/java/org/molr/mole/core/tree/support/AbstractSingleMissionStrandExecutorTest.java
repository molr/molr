package org.molr.mole.core.tree.support;

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
import org.molr.mole.core.tree.tracking.TreeTracker;
import org.molr.mole.core.tree.TreeStructure;

/**
 * Abstract support class for a test against one mission, specified via the {@link #mission()} abstract method.
 */
public abstract class AbstractSingleMissionStrandExecutorTest implements SingleMissionStrandExecutorTestSupport,
        MissionCreationTestSupport, LatchTestSupport {

    private TreeStructure treeStructure;
    private TreeTracker resultTracker;
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

    public Result currentRootResult() {
        return treeResultTracker().resultFor(treeStructure().rootBlock());
    }

    @Override
    public StrandExecutor rootStrandExecutor() {
        return strandExecutor;
    }

    @Override
    public TreeTracker<Result> treeResultTracker() {
        return resultTracker;
    }

    public TreeStructure treeStructure() {
        return treeStructure;
    }

    public LeafExecutor leafExecutor() {
        return leafExecutor;
    }

    public StrandFactory strandFactory() {
        return strandFactory;
    }

    public StrandExecutorFactory strandExecutorFactory() {
        return strandExecutorFactory;
    }

}
