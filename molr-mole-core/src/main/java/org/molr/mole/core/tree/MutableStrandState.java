package org.molr.mole.core.tree;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.Strand;

import static java.util.Objects.requireNonNull;

/**
 * This is a mutable state of a strand! It provides threadsave mutation methods so that it can be used for tracking the state.
 */
public class MutableStrandState {

    private final Strand parent;
    private final Block junctionBlock;

    public MutableStrandState(Strand parent, Block junctionBlock) {
        /* might be null! */
        this.parent = parent;
        this.junctionBlock = requireNonNull(junctionBlock);
    }

    public static MutableStrandState root(Block rootBlock) {
        return new MutableStrandState(null, rootBlock);
    }

    public static MutableStrandState withParent(Strand parent, Block junctionBlock) {
        return new MutableStrandState(requireNonNull(parent, "parent strand must not be null"), junctionBlock);
    }

}
