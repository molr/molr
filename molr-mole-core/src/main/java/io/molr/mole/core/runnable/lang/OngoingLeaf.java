package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Out;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.utils.Checkeds;

import static java.util.Objects.requireNonNull;

public class OngoingLeaf extends OngoingNode<OngoingLeaf> {

    public OngoingLeaf(String name, RunnableLeafsMission.Builder builder, Block parent) {
        super(
                requireNonNull(name, "leafName must not be null"), //
                requireNonNull(builder, "builder must not be null"), //
                requireNonNull(parent, "parent must not be null")
        );
    }

    public Block run(Runnable runnable) {
        return run((in, out) -> runnable.run());
    }

    public Block run(Checkeds.CheckedThrowingRunnable runnable) {
        return run((in, out) -> runnable.run());
    }

    public Block run(Checkeds.CheckedThrowingConsumer<In> runnable) {
        return run((in, out) -> runnable.accept(in));
    }

    public Block run(Checkeds.CheckedThrowingBiConsumer<In, Out> runnable) {
        return builder().leafChild(parent(), name(), runnable, blockAttributes());
    }

}
