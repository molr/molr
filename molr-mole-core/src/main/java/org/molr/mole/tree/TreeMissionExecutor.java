package org.molr.mole.tree;

import com.google.common.collect.ImmutableSet;
import org.molr.commons.api.domain.*;
import org.molr.mole.api.MissionExecutor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.requireNonNull;


/**
 * Keeps track of the state of the execution of one mission instance. It assumes a tree of execution blocks, can execute them and keeps track of cursor positions within strands.
 */
public abstract class TreeMissionExecutor implements MissionExecutor {


    private final AtomicLong nextId = new AtomicLong(0);

    private final Map<Strand, MutableStrandState> strandStates = new ConcurrentHashMap<>();

    private final ReplayProcessor states = ReplayProcessor.cacheLast();
    private final MissionRepresentation representation;

    protected TreeMissionExecutor(MissionRepresentation representation) {
        this.representation = requireNonNull(representation, "representation must not be null");

        MutableStrandState rootState = MutableStrandState.root(representation.rootBlock());
        Strand rootStrand = nextStrand();
        strandStates.put(rootStrand, rootState);

        step(rootStrand, representation.rootBlock());
    }

    private void step(Strand strand, Block block) {
        if (hasChildren(block)) {
            stepInto(strand, block);
        } else {
            execute(strand, block);
        }
    }

    @Override
    public Flux<MissionState> states() {
        return states;
    }

    @Override
    public void instruct(Strand strand, MissionCommand command) {

    }


    private void stepInto(Strand strand, Block block) {
        if (parallelChildren(block)) {
            // think what to do
        }

    }

    protected abstract void execute(Strand strand, Block block);


    private Set<Block> entryPoints() {
        List<Block> firstLevel = representation.childrenOf(representation.rootBlock());
        if (firstLevel.isEmpty()) {
            return Collections.emptySet();
        }
        if (parallelChildren(representation.rootBlock())) {
            return ImmutableSet.copyOf(firstLevel);
        } else {
            return ImmutableSet.of(firstLevel.get(0));
        }
    }

    protected abstract boolean parallelChildren(Block block);

    protected abstract boolean hasChildren(Block block);




    private Strand nextStrand() {
        return Strand.ofId("" + nextId.getAndIncrement());
    }


}
