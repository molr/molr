package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.ctx.OngoingContextualBranch;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.molr.mole.core.runnable.lang.BranchMode.PARALLEL;
import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;
import static java.util.Objects.requireNonNull;

public class OngoingBranch extends OngoingNode<OngoingBranch> {

    private BranchMode mode;
    private final AtomicBoolean asCalled = new AtomicBoolean(false);

    public OngoingBranch(String name, RunnableLeafsMission.Builder builder, Block parent, BranchMode mode) {
        super(
                requireNonNull(name, "branchName must not be null"), //
                requireNonNull(builder, "builder must not be null"), //
                parent /* parent may be null (special case for root branch)*/
        );
        this.mode = requireNonNull(mode);
    }

    public OngoingBranch parallel() {
        this.mode = PARALLEL;
        return this;
    }

    public OngoingBranch sequential() {
        this.mode = SEQUENTIAL;
        return this;
    }

    public <C> OngoingContextualBranch<C> contextual(Function<In, C> contextFactory) {
        return new OngoingContextualBranch<>(name(), builder(), parent(), mode, contextFactory);
    }

    public void as(Consumer<Branch> branchDescription) {
        if (asCalled.getAndSet(true)) {
            throw new IllegalStateException("as() method must only be called once!");
        }
        requireNonNull(branchDescription, "branchDescription must not be null.");

        Block block = block();
        Branch branch = Branch.withParent(builder(), block);
        branchDescription.accept(branch);
    }

    protected Block block() {
        if (parent() == null) {
            return builder().rootBranchNode(name(), mode, blockAttributes());
        } else {
            return builder().childBranchNode(parent(), name(), mode, blockAttributes());
        }
    }

}
