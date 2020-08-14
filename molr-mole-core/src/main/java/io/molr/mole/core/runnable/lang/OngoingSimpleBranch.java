package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.mole.core.runnable.RunnableLeafsMission;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class OngoingSimpleBranch extends GenericOngoingBranch<OngoingSimpleBranch> {

    private final AtomicBoolean asCalled = new AtomicBoolean(false);

    public OngoingSimpleBranch(String name, RunnableLeafsMission.Builder builder, Block parent, BranchMode mode) {
        super(name, builder, parent, mode);
    }

    public OngoingSimpleLeaf leaf(String name) {
        return new OngoingSimpleLeaf(name, builder(), parent());
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
