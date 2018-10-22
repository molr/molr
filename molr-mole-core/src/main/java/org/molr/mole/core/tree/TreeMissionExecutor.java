package org.molr.mole.core.tree;

import org.molr.commons.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import static java.util.Objects.requireNonNull;
import static org.molr.commons.domain.StrandCommand.STEP_INTO;


/**
 * Keeps track of the state of the execution of one mission instance. It assumes a tree of execution blocks, can execute them and keeps track of cursor positions within strands.
 *
 *
 *
 */
public class TreeMissionExecutor implements MissionExecutor {

    private final ReplayProcessor states = ReplayProcessor.cacheLast();
    private final SequentialExecutor rootExecutor;
    private final MutableStrandTracker strandTracker;
    private final Strand rootStrand;
    private final StrandFactoryImpl strandFactory;

    public TreeMissionExecutor(TreeStructure treeStructure, LeafExecutor leafExecutor, ResultTracker resultTracker) {

        strandFactory = new StrandFactoryImpl();
        rootStrand = strandFactory.nextStrand();

        strandTracker = new MutableStrandTracker();
        strandTracker.trackStrand(rootStrand);

        Block rootBlock = treeStructure.rootBlock();
        MutableStrandState rootState = MutableStrandState.root(rootBlock);
        CursorTracker cursorTracker = CursorTracker.ofBlock(rootBlock);
        rootExecutor = new SequentialExecutorImpl(rootStrand, cursorTracker, treeStructure, leafExecutor, resultTracker, strandFactory, strandTracker, new SingleThreadDispacherFactory());

        if (!treeStructure.isLeaf(rootBlock)) {
            rootExecutor.instruct(STEP_INTO);
        }
    }

    @Deprecated
    public Strand getRootStrand() {
        return rootStrand;
    }

    @Deprecated
    public StrandFactoryImpl getStrandFactory() {
        return strandFactory;
    }

    @Override
    public Flux<MissionState> states() {
        return states;
    }

    @Override
    public void instruct(Strand strand, StrandCommand command) {
        SequentialExecutor executor = strandTracker.currentExecutorFor(strand);
        executor.instruct(command);
        //Optional.ofNullable(strandInstances.get(strand)).ifPresent(i -> i.instruct(command));
    }


}
