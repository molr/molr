package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

import java.util.Map;

public class OngoingSimpleBranch extends OngoingContextualOptionProvidingBranch<OngoingSimpleBranch> {

    private final AtomicBoolean asCalled = new AtomicBoolean(false);

    public OngoingSimpleBranch(BlockNameConfiguration name, RunnableLeafsMission.Builder builder, Block parent, BranchMode mode, Map<Placeholder<?>, Function<In, ?>> mappings) {
        super(name, builder, parent, mode, mappings);
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
