package io.molr.mole.core.tree;

import com.google.common.collect.ImmutableSet;
import io.molr.commons.domain.Block;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.Strand;
import io.molr.mole.core.runnable.RunStates;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.molr.commons.domain.RunState.FINISHED;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

/**
 * FIXME to be merged most probably with StrandFactory...
 */
public class StrandExecutorFactory{

    private final Object strandExecutorLock = new Object();
    private final StrandFactory strandFactory;
    private final LeafExecutor leafExecutor;

    // FIXME #1 change to interface!
    private final ConcurrentHashMap<Strand, ConcurrentStrandExecutor> strandExecutors;
    private final EmitterProcessor<StrandExecutor> newStrandsSink;
    private final Flux<StrandExecutor> newStrandsStream;
    private final RunStates runStates;

    public StrandExecutorFactory(StrandFactory strandFactory, LeafExecutor leafExecutor, RunStates runStates) {
        this.strandFactory = requireNonNull(strandFactory, "strandFactory cannot be null");
        this.leafExecutor = requireNonNull(leafExecutor, "leafExecutor cannot be null");
        this.strandExecutors = new ConcurrentHashMap<>();
        this.runStates = runStates;

        newStrandsSink = EmitterProcessor.create();
        Scheduler strandsScheduler = Schedulers.elastic();
        newStrandsStream = newStrandsSink.publishOn(strandsScheduler).doFinally(signal-> {
        	strandsScheduler.dispose();
        });
    }

    public StrandExecutor createStrandExecutor(Strand strand, TreeStructure structure, Set<Block> breakpoints, Set<Block> blocksToBeIgnored, ExecutionStrategy executionStrategy) {
        synchronized (strandExecutorLock) {
            if (strandExecutors.containsKey(strand)) {
                throw new IllegalArgumentException(strand + " is already associated with an executor");
            }
            ConcurrentStrandExecutor strandExecutor = new ConcurrentStrandExecutor(strand, structure.rootBlock(), structure, strandFactory, this, leafExecutor, breakpoints, blocksToBeIgnored, executionStrategy, runStates);
            strandExecutors.put(strand, strandExecutor);
            newStrandsSink.onNext(strandExecutor);
            return strandExecutor;
        }
    }

    public StrandExecutor getStrandExecutorFor(Strand strand) {
        synchronized (strandExecutorLock) {
            if (!strandExecutors.containsKey(strand)) {
                throw new IllegalArgumentException(strand + " is not tracked by this factory");
            }
            return strandExecutors.get(strand);
        }
    }

    public Set<StrandExecutor> allStrandExecutors() {
        synchronized (strandExecutorLock) {
            return ImmutableSet.copyOf(strandExecutors.values());
        }
    }

    public Set<StrandExecutor> activeStrandExecutors() {
        synchronized (strandExecutorLock) {
            return strandExecutors.values().stream().filter(s -> s.getActualState() != FINISHED).collect(toSet());
        }
    }

    public Flux<StrandExecutor> newStrandsStream() {
        return newStrandsStream;
    }
    
    public void closeStrandsStream() {
    	newStrandsSink.onComplete();
    }

}
