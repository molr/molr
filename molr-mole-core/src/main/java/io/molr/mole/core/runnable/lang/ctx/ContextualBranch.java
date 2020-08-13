package io.molr.mole.core.runnable.lang.ctx;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.Branch;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;
import static java.util.Objects.requireNonNull;

public class ContextualBranch<C> extends Branch {

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
