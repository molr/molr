package org.molr.mole.core.tree;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import org.molr.commons.domain.Strand;

import javax.annotation.concurrent.GuardedBy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class StrandFactoryImpl implements StrandFactory {


    private final Object lock = new Object();
    private final Strand rootStrand = strandOfId(0);
    private final AtomicLong nextId = new AtomicLong(1);

    @GuardedBy("lock")
    private final ListMultimap<Strand, Strand> parentToChildren = LinkedListMultimap.create();

    @GuardedBy("lock")
    private final Map<Strand, Strand> childToParent = new HashMap<>();


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
    public Strand parentOf(Strand strand) {
        synchronized (lock) {
            return childToParent.get(strand);
        }
    }
}
