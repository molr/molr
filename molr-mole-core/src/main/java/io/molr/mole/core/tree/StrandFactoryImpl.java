package io.molr.mole.core.tree;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.concurrent.GuardedBy;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import io.molr.commons.domain.Strand;

public class StrandFactoryImpl implements StrandFactory {


    private final Object lock = new Object();
    private final Strand rootStrand = strandOfId(0);
    private final AtomicLong nextId = new AtomicLong(1);

    @GuardedBy("lock")
    private final Map<Strand, Strand> childToParent = new HashMap<>();

    @GuardedBy("lock")
    private final ListMultimap<Strand, Strand> parentToChildren = LinkedListMultimap.create();

    @Override
    public Strand createChildStrand(Strand parent) {
        synchronized (lock) {
            Strand newStrand = nextStrand();
            childToParent.put(newStrand, parent);
            parentToChildren.put(parent, newStrand);
            return newStrand;
        }
    }

    @Override
    public Strand rootStrand() {
        return this.rootStrand;
    }

    private Strand nextStrand() {
        return strandOfId(nextId.getAndIncrement());
    }

    private static final Strand strandOfId(long longId) {
        return Strand.ofId("" + longId);
    }

    @Override
    public Optional<Strand> parentOf(Strand strand) {
        synchronized (lock) {
            if (rootStrand.equals(strand)) {
                return Optional.empty();
            }

            if (childToParent.values().contains(strand) || childToParent.keySet().contains(strand)) {
                return Optional.ofNullable(childToParent.get(strand));
            }

            throw new IllegalArgumentException(strand + " was not created by this factory");
        }
    }

}
