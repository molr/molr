package io.molr.mole.core.runnable.lang.ctx;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholders;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.OngoingLeaf;
import io.molr.mole.core.utils.Checkeds;

public class OngoingContextualLeaf<C> extends OngoingLeaf {

    public OngoingContextualLeaf(String name, RunnableLeafsMission.Builder builder, Block parent) {
        super(name, builder, parent);
    }

    public void ctxRun(Checkeds.CheckedThrowingConsumer<C> runnable) {
        run(in -> {
            C c = in.get(Placeholders.context());
            runnable.accept(c);
        });
    }
}
