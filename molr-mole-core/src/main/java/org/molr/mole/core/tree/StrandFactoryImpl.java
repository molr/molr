package org.molr.mole.core.tree;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import org.molr.commons.domain.Strand;

import javax.annotation.concurrent.GuardedBy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class StrandFactoryImpl implements StrandFactory {

    private final Object lock = new Object();

    @GuardedBy("lock")
    private final ListMultimap<Strand, Strand> parentToChildren = LinkedListMultimap.create();
    private final AtomicLong nextId = new AtomicLong();

    @Override
    public Strand createChildStrand(Strand parent) {
        synchronized (lock) {
            Strand newStrand = nextStrand();
            parentToChildren.put(parent, newStrand);
            return newStrand;
        }
    }

    public Strand nextStrand() {
        return Strand.ofId("" + nextId.getAndIncrement());
    }

}
