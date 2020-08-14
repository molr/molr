package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.ctx.OngoingContextualBranch;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class OngoingRootBranch extends GenericOngoingBranch<OngoingRootBranch> {

    private final AtomicBoolean asCalled = new AtomicBoolean(false);

    public OngoingRootBranch(String name, RunnableLeafsMission.Builder builder, Block parent, BranchMode mode) {
        super(name, builder, parent, mode);
    }

    public <C> OngoingContextualBranch<C> contextual(Function<In, C> contextFactory) {
        builder().contextFactory(contextFactory);
        return new OngoingContextualBranch<>(name(), builder(), parent(), mode());
    }

    public <C, P1> OngoingContextualBranch<C> contextual(Function<P1, C> contextFactory, Placeholder<P1> p1) {
        return contextual(in -> contextFactory.apply(in.get(p1)));
    }

    public <C, P1, P2> OngoingContextualBranch<C> contextual(BiFunction<P1, P2, C> contextFactory, Placeholder<P1> p1, Placeholder<P2> p2) {
        return contextual(in -> contextFactory.apply(in.get(p1), in.get(p2)));
    }


    public void as(Consumer<SimpleBranch> branchDescription) {
        if (asCalled.getAndSet(true)) {
            throw new IllegalStateException("as() method must only be called once!");
        }
        requireNonNull(branchDescription, "branchDescription must not be null.");

        Block block = block();
        SimpleBranch branch = SimpleBranch.withParent(builder(), block);
        branchDescription.accept(branch);
    }

}
