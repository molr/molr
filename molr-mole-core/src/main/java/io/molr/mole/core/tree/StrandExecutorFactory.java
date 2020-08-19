package io.molr.mole.core.tree;

import com.google.common.collect.ImmutableSet;
import io.molr.commons.domain.Block;
import io.molr.commons.domain.Strand;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.molr.commons.domain.RunState.FINISHED;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

/**
 * FIXME to be merged most probably with StrandFactory...
 */
public class StrandExecutorFactory {

    private final Object strandExecutorLock = new Object();
    private final StrandFactory strandFactory;
    private final LeafExecutor leafExecutor;

    // FIXME #1 change to interface!
    private final ConcurrentHashMap<Strand, ConcurrentStrandExecutor> strandExecutors;
    private final EmitterProcessor<StrandExecutor> newStrandsSink;
    private final Flux<StrandExecutor> newStrandsStream;

    public StrandExecutorFactory(StrandFactory strandFactory, LeafExecutor leafExecutor) {
        this.strandFactory = requireNonNull(strandFactory, "strandFactory cannot be null");
        this.leafExecutor = requireNonNull(leafExecutor, "leafExecutor cannot be null");
        this.strandExecutors = new ConcurrentHashMap<>();

        newStrandsSink = EmitterProcessor.create();
        newStrandsStream = newStrandsSink.publishOn(Schedulers.elastic());
    }

    public StrandExecutor createStrandExecutor(Strand strand, TreeStructure structure, Set<Block> breakpoints, boolean lenientMode) {
        synchronized (strandExecutorLock) {
            if (strandExecutors.containsKey(strand)) {
                throw new IllegalArgumentException(strand + " is already associated with an executor");
            }
            ConcurrentStrandExecutor strandExecutor = new ConcurrentStrandExecutor(strand, structure.rootBlock(), structure, strandFactory, this, leafExecutor, breakpoints, lenientMode);
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

    /**
     * DO NOT USE! jUST FOR DEBUGGING FIXME remove this method
     */
    @Deprecated
    public Optional<StrandExecutor> _getStrandExecutorByStrandId(String id) {
        synchronized (strandExecutorLock) {
            return strandExecutors.entrySet().stream()
                    .filter(entry -> entry.getKey().id().equals(id))
                    .findFirst()
                    .map(entry -> (StrandExecutor) entry.getValue()); // FIXME redundant cast to make the compiler happy.. to be removed when #1 fixed
        }
    }

}
