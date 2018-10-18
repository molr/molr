package org.molr.mole.core.tree;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;
import static org.molr.commons.domain.RunState.FINISHED;
import static org.molr.commons.domain.RunState.PAUSED;
import static org.molr.commons.domain.RunState.RUNNING;

public abstract class SequentialExecutor {

    private final ExecutorService runExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService stepExecutor = Executors.newSingleThreadExecutor();

    private final AtomicBoolean shallRun = new AtomicBoolean(false);
    private CyclicBarrier stepIdle = new CyclicBarrier(1);


    private final Strand strand;

    private final AtomicReference<Block> cursor;
    private final AtomicReference<RunState> runState = new AtomicReference<>(PAUSED);


    public SequentialExecutor(Strand strand, Block firstBlock) {
        this.strand = requireNonNull(strand, "strand must not be null");
        cursor = new AtomicReference<>(requireNonNull(firstBlock, "first block must not be null"));
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

    public void stepInto() {

    }

    private void doStepOver() {
        doBlock(cursor.get());

        if (allowedToContinue()) {
            moveNext();
        }
    }


    private void moveNext() {
        Optional<Block> nextBlock = next();
        if (nextBlock.isPresent()) {
            cursor.set(nextBlock.get());
        } else {
            cursor.set(null);
            runState.set(FINISHED);
        }
    }

    private void assertPaused(String s) {
        RunState state = runState.get();
        if (!PAUSED.equals(state)) {
            throw new IllegalStateException(s + " However the state of the strand was " + state);
        }
    }

    protected void doBlock(Block block) {
        if (isLeaf(block)) {
            executeLeaf(block);
        } else if (parallelBlock(block)) {
            for (Block child : childrenOf(block)) {
                Strand childStrand = newStrand();

                /* TODO: keep track of children! */
                SequentialExecutor executor = sequExecutor(childStrand, child);
                executor.resume();
            }
        } else {
            for (Block child : childrenOf(block)) {
                SequentialExecutor executor = sequExecutor(strand, child);
                executor.resume();
            }
        }
    }

    protected abstract List<Block> childrenOf(Block block);

    protected abstract boolean parallelBlock(Block block);


    protected abstract boolean isLeaf(Block block);


    public void instruct(StrandCommand command) {

    }

    protected abstract Optional<Block> next();

    protected abstract void executeLeaf(Block leaf);

    protected abstract Strand newStrand();

    protected abstract boolean allowedToContinue();

    public static SequentialExecutor sequExecutor(Strand strand, Block firstBlock) {
        return null;
    }


}
