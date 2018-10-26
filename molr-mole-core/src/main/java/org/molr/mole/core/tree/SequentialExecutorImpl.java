package org.molr.mole.core.tree;

import com.google.common.collect.ImmutableSet;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Collections.newSetFromMap;
import static java.util.Objects.requireNonNull;
import static org.molr.commons.domain.Result.SUCCESS;
import static org.molr.commons.domain.RunState.FINISHED;
import static org.molr.commons.domain.RunState.PAUSED;
import static org.molr.commons.domain.RunState.RUNNING;
import static org.molr.commons.domain.StrandCommand.PAUSE;
import static org.molr.commons.domain.StrandCommand.RESUME;
import static org.molr.commons.domain.StrandCommand.SKIP;
import static org.molr.commons.domain.StrandCommand.STEP_INTO;
import static org.molr.commons.domain.StrandCommand.STEP_OVER;

/*
TODO: pause/resume child states
 */
public class SequentialExecutorImpl implements SequentialExecutor {

    private final static Logger LOGGER = LoggerFactory.getLogger(SequentialExecutorImpl.class);


    private final Set<SequentialExecutor> childrenExecutors = newSetFromMap(new ConcurrentHashMap<>());

    private final AtomicBoolean shallRun = new AtomicBoolean(false);

    private final Strand strand;

    private final CompletableFuture<Void> end = new CompletableFuture<>();

    private final CursorTracker cursorTracker;
    private final TreeStructure treeStructure;
    private final LeafExecutor leafExecutor;
    private final ResultTracker resultTracker;
    private final StrandFactory strandFactory;
    private final MutableStrandTracker strandTracker;

    private final AtomicReference<RunState> runState = new AtomicReference<>(PAUSED);
    private final DispacherFactory dispatcherFactory;
    private final CommandDispatcher dispatcher;

    public SequentialExecutorImpl(Strand strand, CursorTracker cursorTracker, TreeStructure treeStructure, LeafExecutor leafExecutor, ResultTracker resultTracker, StrandFactory strandFactory, MutableStrandTracker strandTracker, DispacherFactory dispatcherFactory) {
        this.strand = requireNonNull(strand, "strand must not be null");
        this.cursorTracker = requireNonNull(cursorTracker, "cursorTracker must not be null");
        this.treeStructure = requireNonNull(treeStructure, "treeStructure must not be null");
        this.leafExecutor = requireNonNull(leafExecutor, "leafExecutor must not be null");
        this.resultTracker = requireNonNull(resultTracker, "resultTracker must not be null");
        this.strandFactory = requireNonNull(strandFactory, "strandFactory must not be null");
        this.strandTracker = requireNonNull(strandTracker, "strandTracker must not be null");
        this.dispatcherFactory = requireNonNull(dispatcherFactory, "dispatcherFactory must not be null");


        strandTracker.setCurrentExecutorFor(strand, this);
        this.dispatcher = this.dispatcherFactory.createDispatcher(strand, this::dispatch);
    }

    private void resume() {
        if (!isPaused("Can resume only when paused.")) {
            return;
        }

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

    private void pause() {
        shallRun.set(false);
    }

    private void stepOver() {
        if (!isPaused("Can step only when paused.")) {
            return;
        }
        doStepOver();
    }

    private void skip() {
        moveNext();
    }


    /**
     * Side effect free, just create the sub executors and create the new cursors.
     */
    private void stepInto() {
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

        createAndReactOnChildExecutors(block);
    }

    private List<SequentialExecutor> createAndReactOnChildExecutors(Block block) {
        List<SequentialExecutor> childExecutors = childExecutors(block);
        CompletableFuture[] futures = childExecutors.stream().map(e -> e.end()).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).thenRun(this::moveNextOrPause);
        return childExecutors;
    }


    private List<SequentialExecutor> childExecutors(Block parent) {
        if (treeStructure.isParallel(parent)) {
            return treeStructure.childrenOf(parent).stream().map(child -> {
                Strand newStrand = strandFactory.createChildStrand(strand);
                strandTracker.trackStrand(newStrand);
                return newChildExecutor(newStrand, Collections.singletonList(child));
            }).collect(Collectors.toList());
        } else {
            return Collections.singletonList(newChildExecutor(strand, treeStructure.childrenOf(parent)));
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
            moveNextOrPause();
        } else {
            List<SequentialExecutor> childExecutors = createAndReactOnChildExecutors(block);
            childExecutors.forEach(e -> e.instruct(RESUME));
        }
    }

    private void moveNextOrPause() {
        if (allowedToContinue()) {
            moveNext();
        } else {
            pause();
        }
    }


    private void moveNext() {
        Optional<Block> nextBlock = cursorTracker.moveNext();
        if (!nextBlock.isPresent()) {
            finishExecutor();
        }
    }

    private void finishExecutor() {
        runState.set(FINISHED);
        strandTracker.unsetCurrentExecutorFor(strand, this);
        end.complete(null);
    }


    private boolean isPaused(String message) {
        RunState state = runState.get();
        if (PAUSED.equals(state)) {
            return true;
        } else {
            LOGGER.warn(message + " However the state of the strand was " + state);
            return false;
        }
    }

    private SequentialExecutor newChildExecutor(Strand strand, List<Block> blocks) {
        SequentialExecutor childExecutor = sequExecutor(strand, CursorTracker.ofBlocks(blocks));
        this.childrenExecutors.add(childExecutor);
        return childExecutor;
    }

    /* XXX to be seen if this is the right way... Is there a runstate per strand or better per node... or both?*/
    public RunState runState() {
        return runState.get();
    }

    @Override
    public Block cursor() {
        return cursorTracker.actual().orElse(null);
    }

    @Override
    public Set<StrandCommand> allowedCommands() {
        return allowedCommands(cursorTracker.actual(), runState());
    }

    private Set<StrandCommand> allowedCommands(Optional<Block> cursor, RunState runState) {
        if (!cursor.isPresent()) {
            return Collections.emptySet();
        }

        ImmutableSet.Builder<StrandCommand> builder = ImmutableSet.builder();
        switch (runState) {
            case PAUSED:
                builder.add(RESUME, STEP_OVER, SKIP);
                if (!treeStructure.isLeaf(cursor.get())) {
                    builder.add(STEP_INTO);
                }
                break;
            case RUNNING:
                builder.add(PAUSE);
                break;
        }
        return builder.build();
    }


    private void dispatch(StrandCommand command) {
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


    @Override
    public void instruct(StrandCommand command) {
        dispatcher.instruct(command);
    }

    @Override
    public CompletableFuture<Void> end() {
        return this.end;
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
        return new SequentialExecutorImpl(newStrand, newCursorTracker, treeStructure, leafExecutor, resultTracker, strandFactory, strandTracker, dispatcherFactory);
    }

}
