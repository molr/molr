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
public abstract class TreeMissionExecutor implements MissionExecutor {

    private final MissionRepresentation representation;


    private final Map<Strand, SequentialExecutor> strandInstances = new ConcurrentHashMap<>();


    private final Map<Strand, MutableStrandState> strandStates = new ConcurrentHashMap<>();


    private final Map<Strand, Block> strandCursorPositions = new ConcurrentHashMap<>();
    private final Map<Strand, RunState> strandRunStates = new ConcurrentHashMap<>();


    private final AtomicLong nextId = new AtomicLong(0);
    private final ReplayProcessor states = ReplayProcessor.cacheLast();


    protected TreeMissionExecutor(MissionRepresentation representation) {
        this.representation = requireNonNull(representation, "representation must not be null");

        Block rootBlock = representation.rootBlock();
        MutableStrandState rootState = MutableStrandState.root(rootBlock);
        Strand rootStrand = nextStrand();
        strandStates.put(rootStrand, rootState);
        strandCursorPositions.put(rootStrand, rootBlock);

        if (!isLeaf(rootBlock)) {
            stepInto(rootStrand, rootBlock);
        }
    }


    private void stepInto(Strand strand, Block block) {
//        if (isLeaf(block)) {
//            execute(strand, block);
//        } else
//
        if (parallelChildren(block)) {
            for (Block child : childrenOf(block)) {
                Strand newStrand = nextStrand();
                strandCursorPositions.put(newStrand, child);
                //run(newStrand, block);
            }
        } else {
            List<Block> children = childrenOf(block);
            Block firstChild = children.get(0);
            strandCursorPositions.put(strand, firstChild);

        }

    }

    private void run(Strand strand, Block block) {
        if (isLeaf(block)) {
            execute(strand, block);
        } else {
            runInto(strand, block);
        }
    }

    private void runInto(Strand strand, Block block) {
        if (parallelChildren(block)) {
            for (Block child : childrenOf(block)) {
                Strand newStrand = nextStrand();
                run(newStrand, block);
            }
        } else {
            for (Block child : childrenOf(block)) {
                run(strand, block);
            }
        }
    }

    @Override
    public Flux<MissionState> states() {
        return states;
    }

    @Override
    public void instruct(Strand strand, StrandCommand command) {
        Optional.ofNullable(strandInstances.get(strand)).ifPresent(i -> i.instruct(command));
    }

    protected abstract boolean parallelChildren(Block block);


    private boolean isLeaf(Block block) {
        return childrenOf(block).isEmpty();
    }

    private List<Block> childrenOf(Block block) {
        return representation.childrenOf(block);
    }


    private Strand nextStrand() {
        return Strand.ofId("" + nextId.getAndIncrement());
    }


    /**
     * This finally delegates the real exewcution to something else. This is called fcr leaves only.
     *
     * @param strand
     * @param block
     */
    protected abstract void execute(Strand strand, Block block);

}
