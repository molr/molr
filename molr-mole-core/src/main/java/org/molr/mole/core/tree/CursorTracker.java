package org.molr.mole.core.tree;

import com.google.common.collect.ImmutableList;
import org.molr.commons.domain.Block;

import javax.annotation.concurrent.GuardedBy;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

/**
 * Keeps track of the cursor position within one sequential branch of a tree.
 * <p>
 * This class is threadsafe
 */
public class CursorTracker {

    private final Object monitor = new Object();

    private final List<Block> sequentialBlocks;

    @GuardedBy("monitor")
    private int cursor = 0;

    private CursorTracker(List<Block> sequentialBlocks) {
        this.sequentialBlocks = ImmutableList.copyOf(requireNonNull(sequentialBlocks, "sequentialBlocks must not be null"));
        if (sequentialBlocks.isEmpty()) {
            throw new IllegalArgumentException("sequential blocks must not be empty!");
        }
    }

    public static CursorTracker ofBlocks(List<Block> sequentialBlocks) {
        return new CursorTracker(sequentialBlocks);
    }

    public static CursorTracker ofBlock(Block singleBlock) {
        return ofBlocks(Collections.singletonList(singleBlock));
    }

    public Optional<Block> actual() {
        synchronized (monitor) {
            if (cursor < 0) {
                return Optional.empty();
            } else {
                return Optional.of(sequentialBlocks.get(cursor));
            }
        }
    }

    public Optional<Block> moveNext() {
        synchronized (monitor) {
            if (cursor < 0) {
                return Optional.empty();
            }
            cursor += 1;
            if (cursor > (sequentialBlocks.size() - 1)) {
                cursor = -1;
            }
            return actual();
        }
    }


}