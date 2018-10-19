package org.molr.mole.core.tree;

import org.molr.commons.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.newSetFromMap;
import static java.util.Objects.requireNonNull;
import static org.molr.commons.domain.Result.SUCCESS;
import static org.molr.commons.domain.RunState.FINISHED;
import static org.molr.commons.domain.RunState.PAUSED;
import static org.molr.commons.domain.RunState.RUNNING;

/*
TODO: pause/resume child states
 */
public class SequentialExecutor {

    private static Logger LOGGER = LoggerFactory.getLogger(SequentialExecutor.class);

    private final Set<SequentialExecutor> childrenExecutors = newSetFromMap(new ConcurrentHashMap<>());

    private final AtomicBoolean shallRun = new AtomicBoolean(false);

    private final Strand strand;

    private final AtomicReference<RunState> runState = new AtomicReference<>(PAUSED);

    private final CursorTracker cursorTracker;
    private final TreeStructure treeStructure;
    private final LeafExecutor leafExecutor;
    private final ResultTracker resultTracker;
    private final StrandFactory strandFactory;

    public SequentialExecutor(Strand strand, CursorTracker cursorTracker, TreeStructure treeStructure, LeafExecutor leafExecutor, ResultTracker resultTracker, StrandFactory strandFactory) {
        this.strand = requireNonNull(strand, "strand must not be null");
        this.cursorTracker = requireNonNull(cursorTracker, "cursorTracker must not be null");
        this.treeStructure = requireNonNull(treeStructure, "treeStructure must not be null");
        this.leafExecutor = requireNonNull(leafExecutor, "leafExecutor must not be null");
        this.resultTracker = requireNonNull(resultTracker, "resultTracker must not be null");
        this.strandFactory = requireNonNull(strandFactory, "strandFactory must not be null");
    }


    private void resume() {
        assertPaused("Can resume only when paused.");

        runState.set(RUNNING);
        shallRun.set(true);

        while (RUNNING.equals(runState.get())) {
            if (shallRun.get()) {
                doStepOver();
            } else {
                runState.set(PAUSED);
            }
        }
    }

    public void pause() {
        shallRun.set(false);
    }

    public void stepOver() {
        assertPaused("Can step only when paused.");
        doStepOver();
    }

    public void skip() {
        moveNext();
    }


    /**
     * Side effect free, just create the sub executors and create the new cursors.
     */
    public void stepInto() {
        Optional<Block> blockOptional = cursorTracker.actual();
        if (!blockOptional.isPresent()) {
            LOGGER.warn("No actual block. Cannot step over");
            return;
        }

        Block block = blockOptional.get();
        if (treeStructure.isLeaf(block)) {
            LOGGER.warn("Step into leave '" + block + "'is not allowed.");
            return;
        }

        if (treeStructure.isParallel(block)) {
            for (Block child : treeStructure.childrenOf(block)) {
                newChildExecutor(strandFactory.createChildStrand(strand), Collections.singletonList(child));
            }
        } else {
            newChildExecutor(strand, treeStructure.childrenOf(block));
        }
    }


    private void doStepOver() {
        Optional<Block> blockOptional = cursorTracker.actual();
        if (!blockOptional.isPresent()) {
            LOGGER.warn("No actual block. Cannot step over");
            return;
        }
        Block block = blockOptional.get();
        if (treeStructure.isLeaf(block)) {
            leafExecutor.execute(block);
        } else if (treeStructure.isParallel(block)) {
            for (Block child : treeStructure.childrenOf(block)) {
                runSubTree(strandFactory.createChildStrand(strand), Collections.singletonList(child));
            }
        } else {
            runSubTree(strand, treeStructure.childrenOf(block));
        }

        if (allowedToContinue()) {
            moveNext();
        } else {
            pause();
        }
    }


    private void moveNext() {
        Optional<Block> nextBlock = cursorTracker.moveNext();
        if (!nextBlock.isPresent()) {
            runState.set(FINISHED);
        }
    }


    private void assertPaused(String message) {
        RunState state = runState.get();
        if (!PAUSED.equals(state)) {
            throw new IllegalStateException(message + " However the state of the strand was " + state);
        }
    }

    private Block firstChildOf(Block block) {
        List<Block> children = treeStructure.childrenOf(block);
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Block " + block + " has no children. Usually this call should not happen.");
        }
        return children.get(0);
    }

    private void runSubTree(Strand strand, List<Block> blocks) {
        SequentialExecutor childExecutor = newChildExecutor(strand, blocks);
        childExecutor.resume();
    }

    private SequentialExecutor newChildExecutor(Strand strand, List<Block> blocks) {
        SequentialExecutor childExecutor = sequExecutor(strand, CursorTracker.ofBlocks(blocks));
        this.childrenExecutors.add(childExecutor);
        return childExecutor;
    }

    public void instruct(StrandCommand command) {
        switch (command) {
            case PAUSE:
                pause();
                return;
            case SKIP:
                skip();
                return;
            case RESUME:
                resume();
                return;
            case STEP_INTO:
                stepInto();
                return;
            case STEP_OVER:
                stepOver();
                return;
        }
        throw new IllegalArgumentException("Command '" + command + "' could not be interpreted.");
    }

    private boolean allowedToContinue() {
        /* TODO review thread safetyness ;-)*/
        Optional<Block> actualOptional = cursorTracker.actual();
        if (!actualOptional.isPresent()) {
            return false;
        }
        return SUCCESS.equals(resultTracker.resultFor(actualOptional.get()));
    }

    private SequentialExecutor sequExecutor(Strand newStrand, CursorTracker newCursorTracker) {
        return new SequentialExecutor(newStrand, newCursorTracker, treeStructure, leafExecutor, resultTracker, strandFactory);
    }

}
