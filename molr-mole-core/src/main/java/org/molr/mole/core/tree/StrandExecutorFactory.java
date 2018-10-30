package org.molr.mole.core.tree;

import com.google.common.collect.ImmutableSet;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.RunState;
import org.molr.commons.domain.Strand;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * FIXME to be merged most probably with StrandFactory...
 */
public class StrandExecutorFactory {

    private final Object strandExecutorLock = new Object();
    private final StrandFactory strandFactory;
    private final LeafExecutor leafExecutor;
    // FIXME #1 change to interface!
    private ImmutableSet<StandaloneStrandExecutor> strandExecutors;

    public StrandExecutorFactory(StrandFactory strandFactory, LeafExecutor leafExecutor) {
        this.strandFactory = requireNonNull(strandFactory, "strandFactory cannot be null");
        this.leafExecutor = requireNonNull(leafExecutor, "leafExecutor cannot be null");
        this.strandExecutors = ImmutableSet.of();
    }

    public StrandExecutor createStrandExecutor(Strand strand, Block block, TreeStructure substructure) {
        synchronized (strandExecutorLock) {
            StandaloneStrandExecutor newStrand = new StandaloneStrandExecutor(strand, block, substructure, strandFactory, this, leafExecutor);
            addStrandExecutor(newStrand);
            return newStrand;
        }
    }

    public Set<StrandExecutor> activeStrandExecutors() {
        synchronized (strandExecutorLock) {
            return strandExecutors.stream().filter(s -> s.getState() == RunState.FINISHED).collect(Collectors.toSet());
        }
    }

    /**
     * DO NOT USE! jUST FOR DEBUGGING
     * FIXME remove this method
     */
    @Deprecated
    public Optional<StrandExecutor> _getStrandExecutorByStrandId(String id) {
        synchronized (strandExecutorLock) {
            return strandExecutors.stream().filter(se -> se.getStrand().id().equals(id)).findFirst()
                    .map(se -> (StrandExecutor) se); // FIXME redundant cast to make the compiler happy.. to be removed when #1 fixed
        }
    }

    private void addStrandExecutor(StandaloneStrandExecutor strandExecutor) {
        strandExecutors = ImmutableSet.<StandaloneStrandExecutor>builder().addAll(strandExecutors).add(strandExecutor).build();
    }

}
