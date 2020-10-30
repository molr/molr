package io.molr.mole.core.runnable.lang.ctx;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.GenericOngoingBranch;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class OngoingContextualBranch<C> extends GenericOngoingBranch<OngoingContextualBranch<C>> {

    private final AtomicBoolean asCalled = new AtomicBoolean(false);
    Placeholder<C> contextPlaceholder;
    boolean newContext = false;

    public OngoingContextualBranch(String name, RunnableLeafsMission.Builder builder, Block parent, BranchMode mode, Placeholder<C> contextPlaceholder, boolean newContext) {
        super(name, builder, parent, mode);
        this.contextPlaceholder = contextPlaceholder;
        this.newContext = newContext;
    }

    public void as(BiConsumer<ContextualBranch<C>,Placeholder<C>> branchDescription) {
        if (asCalled.getAndSet(true)) {
            throw new IllegalStateException("as() method must only be called once!");
        }
        requireNonNull(branchDescription, "branchDescription must not be null.");

        Block block = block();
        ContextualBranch<C> branch = new ContextualBranch<>(builder(), block, contextPlaceholder);
        branchDescription.accept(branch, contextPlaceholder);
    }
}
