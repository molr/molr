package org.molr.mole.core.tree;

import org.molr.commons.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.time.Duration;
import java.util.Collections;

import static java.util.Objects.requireNonNull;
import static org.molr.commons.domain.StrandCommand.STEP_INTO;


/**
 * Keeps track of the state of the execution of one mission instance. It assumes a tree of execution blocks, can execute them and keeps track of cursor positions within strands.
 */
public class TreeMissionExecutor implements MissionExecutor {

    private final Flux<MissionState> states = Flux.interval(Duration.ofSeconds(1)).map(l -> state()).cache(1);
    private final SequentialExecutor rootExecutor;
    private final MutableStrandTracker strandTracker;
    private final Strand rootStrand;
    private final StrandFactoryImpl strandFactory;

    public TreeMissionExecutor(TreeStructure treeStructure, LeafExecutor leafExecutor, ResultTracker resultTracker) {

        strandFactory = new StrandFactoryImpl();
        rootStrand = strandFactory.rootStrand();

        strandTracker = new MutableStrandTracker();
        strandTracker.trackStrand(rootStrand);

        Block rootBlock = treeStructure.rootBlock();
        MutableStrandState rootState = MutableStrandState.root(rootBlock);
        CursorTracker cursorTracker = CursorTracker.ofBlock(rootBlock);
        rootExecutor = new SequentialExecutorImpl(rootStrand, cursorTracker, treeStructure, leafExecutor, resultTracker, strandFactory, strandTracker, new SingleThreadDispatcherFactory());

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


    MissionState state() {
        MissionState.Builder builder = MissionState.builder();
        for (Strand strand : strandTracker.activeStrands()) {
            addStrand(builder, strand);
        }
        return builder.build();
    }

    private void addStrand(MissionState.Builder builder, Strand strand) {
        SequentialExecutor executor = strandTracker.currentExecutorFor(strand);
        RunState runState = executor.runState();
        Block cursor = executor.cursor();
        Strand parent = strandFactory.parentOf(strand);
        builder.add(strand, runState, cursor, parent, executor.allowedCommands());
    }

    @Override
    public void instruct(Strand strand, StrandCommand command) {
        SequentialExecutor executor = strandTracker.currentExecutorFor(strand);
        executor.instruct(command);
    }


}
