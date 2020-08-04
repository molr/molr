package io.molr.mole.core.runnable.lang.ctx;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.OngoingLeaf;
import io.molr.mole.core.utils.Checkeds;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class OngoingContextualLeaf<C> extends OngoingLeaf {

    private final Function<In, C> contextFactory;

    public OngoingContextualLeaf(String name, RunnableLeafsMission.Builder builder, Block parent, Function<In, C> contextFactory) {
        super(name, builder, parent);
        this.contextFactory = requireNonNull(contextFactory, "contextFactory must not be null.");
    }

    public void ctxRun(Checkeds.CheckedThrowingConsumer<C> runnable) {

    }
}
