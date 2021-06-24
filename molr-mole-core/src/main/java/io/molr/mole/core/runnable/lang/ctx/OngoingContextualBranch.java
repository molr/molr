package io.molr.mole.core.runnable.lang.ctx;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.GenericOngoingBranch;
import io.molr.mole.core.runnable.lang.BlockNameConfiguration;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class OngoingContextualBranch<C> extends GenericOngoingBranch<OngoingContextualBranch<C>> {

    private final AtomicBoolean asCalled = new AtomicBoolean(false);
    Placeholder<C> contextPlaceholder;

    public OngoingContextualBranch(BlockNameConfiguration name, RunnableLeafsMission.Builder builder, Block parent, BranchMode mode, Placeholder<C> contextPlaceholder) {
        super(name, builder, parent, mode);
        requireNonNull(contextPlaceholder);
        this.contextPlaceholder = contextPlaceholder;
    }

    public void as(Consumer<ContextualBranch<C>> branchDescription) {
        if (asCalled.getAndSet(true)) {
            throw new IllegalStateException("as() method must only be called once!");
        }
        requireNonNull(branchDescription, "branchDescription must not be null.");

        Block block = block();
        ContextualBranch<C> branch = new ContextualBranch<>(builder(), block, contextPlaceholder);
        branchDescription.accept(branch);
    }
    
    public void as(BiConsumer<ContextualBranch<C>, Placeholder<C>> branchDescription) {
        if (asCalled.getAndSet(true)) {
            throw new IllegalStateException("as() method must only be called once!");
        }
        requireNonNull(branchDescription, "branchDescription must not be null.");

        Block block = block();
        ContextualBranch<C> branch = new ContextualBranch<>(builder(), block, contextPlaceholder);
        branchDescription.accept(branch, contextPlaceholder);
    }
    
    // TODO combine with other contextual providing ongoingBranches (OngoingRoot and OngoingSimple)
    public <C2> OngoingContextualBranchWithNewContext<C2> contextual(Function<In, C2> contextFactory) {
        return new OngoingContextualBranchWithNewContext<>(name(), builder(), parent(), mode(), contextFactory);
    }

    public <C2, P1> OngoingContextualBranchWithNewContext<C2> contextual(Function<P1, C2> contextFactory, Placeholder<P1> p1) {
        return contextual(in -> contextFactory.apply(in.get(p1)));
    }

    public <C2, P1, P2> OngoingContextualBranchWithNewContext<C2> contextual(BiFunction<P1, P2, C2> contextFactory, Placeholder<P1> p1, Placeholder<P2> p2) {
        return contextual(in -> contextFactory.apply(in.get(p1), in.get(p2)));
    }
}
