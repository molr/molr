package io.molr.mole.core.runnable.lang.ctx;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.OngoingContextualOptionProvidingBranch;
import io.molr.mole.core.runnable.lang.BlockNameConfiguration;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class OngoingContextualBranch<C> extends OngoingContextualOptionProvidingBranch<OngoingContextualBranch<C>> {

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

}
