package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Out;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.utils.Checkeds;

import static java.util.Objects.requireNonNull;

public abstract class GenericOngoingLeaf<L extends GenericOngoingLeaf<L>> extends OngoingNode<L> {

    public GenericOngoingLeaf(String name, RunnableLeafsMission.Builder builder, Block parent) {
        super(
                requireNonNull(name, "leafName must not be null"), //
                requireNonNull(builder, "builder must not be null"), //
                requireNonNull(parent, "parent must not be null")
        );
    }
    
    public void run(Runnable runnable) {
        run((in, out) -> runnable.run());
    }

    public void run(Checkeds.CheckedThrowingRunnable runnable) {
        run((in, out) -> runnable.run());
    }

    public void run(Checkeds.CheckedThrowingConsumer<In> runnable) {
        run((in, out) -> runnable.accept(in));
    }

    public void run(Checkeds.CheckedThrowingBiConsumer<In, Out> runnable) {
        builder().leafChild(parent(), name(), runnable, blockAttributes());
    }

}
