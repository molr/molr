package io.molr.mole.core.runnable.lang.ctx;

import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;

import io.molr.commons.domain.Block;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.AbstractBranch;

public class ContextualBranch<C> extends AbstractBranch {

    protected ContextualBranch(RunnableLeafsMission.Builder builder, Block parent) {
        super(builder, parent);
    }

    @Override
    public OngoingContextualBranch<C> branch(String name) {
        return new OngoingContextualBranch<>(name, builder(), parent(), SEQUENTIAL);
    }

    @Override
    public OngoingContextualLeaf<C> leaf(String name) {
        return new OngoingContextualLeaf<>(name, builder(), parent());
    }


}
