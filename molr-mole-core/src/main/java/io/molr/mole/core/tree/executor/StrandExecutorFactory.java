package io.molr.mole.core.tree.executor;

import static io.molr.commons.domain.RunState.FINISHED;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.Strand;
import io.molr.mole.core.tree.LeafExecutor;
import io.molr.mole.core.tree.StrandExecutor;
import io.molr.mole.core.tree.StrandFactory;
import io.molr.mole.core.tree.StrandFactoryImpl;
import io.molr.mole.core.tree.TreeNodeStates;
import io.molr.mole.core.tree.TreeStructure;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * FIXME to be merged most probably with StrandFactory...
 */
public class StrandExecutorFactory{
	
	private final Logger LOGGER = LoggerFactory.getLogger(StrandExecutorFactory.class);

    private final Object strandExecutorLock = new Object();
    private final StrandFactory strandFactory;
    private final LeafExecutor leafExecutor;

    // FIXME #1 change to interface!
    private final ConcurrentHashMap<Strand, ConcurrentStrandExecutor> strandExecutors;
    private final EmitterProcessor<StrandExecutor> newStrandsSink;
    private final Flux<StrandExecutor> newStrandsStream;
    private final TreeNodeStates runStates;

    public StrandExecutorFactory(LeafExecutor leafExecutor, TreeNodeStates runStates) {
        this.leafExecutor = requireNonNull(leafExecutor, "leafExecutor cannot be null");
        this.runStates = requireNonNull(runStates, "runStates must not be null");
        this.strandFactory = new StrandFactoryImpl();
        this.strandExecutors = new ConcurrentHashMap<>();

        newStrandsSink = EmitterProcessor.create();
        Scheduler strandsScheduler = Schedulers.elastic();
        newStrandsStream = newStrandsSink.publishOn(strandsScheduler).doFinally(signal-> {
        	strandsScheduler.dispose();
        });
    }
    
    public Strand rootStrand() {
    	return strandFactory.rootStrand();
    }
    
    public ConcurrentStrandExecutor createRootStrandExecutor(TreeStructure structure, Set<Block> breakpoints, Set<Block> blocksToBeIgnored, ExecutionStrategy executionStrategy) {
    	ConcurrentStrandExecutor rootExecutor = createStrandExecutor(strandFactory.rootStrand(), structure, breakpoints, blocksToBeIgnored, executionStrategy, RunState.PAUSED);
    	rootExecutor.getStateStream().subscribe(state->{}, error->this.closeStrandsStream(), this::closeStrandsStream); 
    	return rootExecutor;
    }
    
    public ConcurrentStrandExecutor createChildStrandExecutor(Strand strand, TreeStructure structure, Set<Block> breakpoints,
    		Set<Block> blocksToBeIgnored, ExecutionStrategy executionStrategy, RunState initialState) {
    	Strand childStrand  = strandFactory.createChildStrand(strand);
    	return createStrandExecutor(childStrand, structure, breakpoints, blocksToBeIgnored, executionStrategy, initialState);
    }

    private ConcurrentStrandExecutor createStrandExecutor(Strand strand, TreeStructure structure, Set<Block> breakpoints, Set<Block> blocksToBeIgnored
    		, ExecutionStrategy executionStrategy, RunState initialState) {
        synchronized (strandExecutorLock) {
            if (strandExecutors.containsKey(strand)) {
                throw new IllegalArgumentException(strand + " is already associated with an executor");
            }
            ConcurrentStrandExecutor strandExecutor = new ConcurrentStrandExecutor(strand, structure.rootBlock(), structure, this, leafExecutor, breakpoints, blocksToBeIgnored, executionStrategy, runStates, initialState);
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
    	LOGGER.info("Close strands stream.");
    	newStrandsSink.onComplete();
    }

	public Optional<Strand> parentOf(Strand strand) {
		return strandFactory.parentOf(strand);
	}

}
