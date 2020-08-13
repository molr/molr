package io.molr.mole.core.runnable.lang.ctx;

import io.molr.commons.domain.Block;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.OngoingBranch;

public class OngoingContextualBranch<C> extends OngoingBranch {

    public OngoingContextualBranch(String name, RunnableLeafsMission.Builder builder, Block parent, BranchMode mode) {
        super(name, builder, parent, mode);
    }
}
