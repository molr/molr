package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.mole.core.runnable.RunnableLeafsMission;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static io.molr.mole.core.runnable.lang.BranchMode.PARALLEL;
import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;
import static java.util.Objects.requireNonNull;

public class OngoingBranch {

    private final String name;
    private final RunnableLeafsMission.Builder builder;
    private final Block parent;

    private BranchMode mode = SEQUENTIAL;
    private final AtomicBoolean asCalled = new AtomicBoolean(false);

    public OngoingBranch(String name, RunnableLeafsMission.Builder builder, Block parent) {
        this.name = requireNonNull(name, "branchName must not be null");
        this.builder = requireNonNull(builder, "builder must not be null");
        this.parent = parent; /* parent may be null (special case for root branch)*/
    }

    public OngoingBranch parallel() {
        this.mode = PARALLEL;
        return this;
    }

    public OngoingBranch sequential() {
        this.mode = SEQUENTIAL;
        return this;
    }

    public Block as(Consumer<Branch> branchDescription) {
        if (asCalled.getAndSet(true)) {
            throw new IllegalStateException("as() method must only be called once!");
        }
        requireNonNull(branchDescription, "branchDescription must not be null.");

        Block block = block();
        Branch branch = Branch.withParent(builder, block);
        branchDescription.accept(branch);
        return block;
    }

    private Block block() {
        if (parent == null) {
            return builder.rootBranchNode(name, mode);
        } else {
            return builder.childBranchNode(parent, name, mode);
        }
    }

}
