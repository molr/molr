package org.molr.mole.core.tree.support;

import org.junit.Before;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.MissionInput;
import org.molr.commons.domain.MissionOutputCollector;
import org.molr.commons.domain.Result;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import org.molr.mole.core.tree.*;

/**
 * Abstract support class for a test against one mission, specified via the {@link #mission()} abstract method.
 */
public abstract class AbstractSingleMissionStrandExecutorTest implements SingleMissionStrandExecutorTestSupport,
        MissionCreationTestSupport, LatchTestSupport {

    private TreeStructure treeStructure;
    private TreeResultTracker resultTracker;
    private LeafExecutor leafExecutor;
    private StrandFactory strandFactory;
    private StrandExecutorFactory strandExecutorFactory;
    private StrandExecutor strandExecutor;

    protected abstract RunnableLeafsMission mission();

    @Before
    public void setUpAbstract() {
        RunnableLeafsMission mission = mission();

        treeStructure = mission.treeStructure();
        resultTracker = new TreeResultTracker(treeStructure.missionRepresentation());
        leafExecutor = new RunnableBlockExecutor(resultTracker, mission.runnables(), MissionInput.empty(), new ConcurrentMissionOutputCollector());
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
    public TreeResultTracker treeResultTracker() {
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
