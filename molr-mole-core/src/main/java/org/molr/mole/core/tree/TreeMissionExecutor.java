package org.molr.mole.core.tree;

import org.molr.commons.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.requireNonNull;


/**
 * Keeps track of the state of the execution of one mission instance. It assumes a tree of execution blocks, can execute them and keeps track of cursor positions within strands.
 *
 *
 *
 */
public class TreeMissionExecutor implements MissionExecutor {

    private final ReplayProcessor states = ReplayProcessor.cacheLast();

    protected TreeMissionExecutor(TreeStructure treeStructure, LeafExecutor leafExecutor, ResultTracker resultTracker) {
        Block rootBlock = treeStructure.rootBlock();
        MutableStrandState rootState = MutableStrandState.root(rootBlock);
        StrandFactoryImpl strandFactory = new StrandFactoryImpl();
        Strand strand = strandFactory.nextStrand();
        CursorTracker cursorTracker = CursorTracker.ofBlock(rootBlock);
        SequentialExecutor rootExecutor = new SequentialExecutor(strand, cursorTracker, treeStructure, leafExecutor, resultTracker, strandFactory);

        if (!treeStructure.isLeaf(rootBlock)) {
            rootExecutor.stepInto();
        }
    }

    @Override
    public Flux<MissionState> states() {
        return states;
    }

    @Override
    public void instruct(Strand strand, StrandCommand command) {
        //Optional.ofNullable(strandInstances.get(strand)).ifPresent(i -> i.instruct(command));
    }


}
