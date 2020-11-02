package io.molr.mole.core.runnable.lang.ctx;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.GenericOngoingBranch;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

public class OngoingContextualBranchWithNewContext<C> extends GenericOngoingBranch<OngoingContextualBranchWithNewContext<C>> {

    private final AtomicBoolean asCalled = new AtomicBoolean(false);
    Function<In,C> contextFactory;

    public OngoingContextualBranchWithNewContext(String name, RunnableLeafsMission.Builder builder, Block parent, BranchMode mode, Function<In,C> contextFactory) {
        super(name, builder, parent, mode);
        this.contextFactory = contextFactory;
    }

    public void as(BiConsumer<ContextualBranch<C>,Placeholder<C>> branchDescription) {
        if (asCalled.getAndSet(true)) {
            throw new IllegalStateException("as() method must only be called once!");
        }
        requireNonNull(branchDescription, "branchDescription must not be null.");
                
        Block block = block();
        @SuppressWarnings("unchecked")
		Placeholder<C> contextPlaceholder = (Placeholder<C>) Placeholder.of(Object.class, UUID.randomUUID().toString());
        builder().contextFactory(block, contextPlaceholder, contextFactory);
        
        ContextualBranch<C> branch = new ContextualBranch<>(builder(), block, contextPlaceholder);
        branchDescription.accept(branch, contextPlaceholder);
    }
}
