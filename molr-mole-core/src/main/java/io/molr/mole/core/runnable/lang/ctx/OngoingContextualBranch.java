package io.molr.mole.core.runnable.lang.ctx;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.Branch;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.OngoingBranch;
import io.molr.mole.core.runnable.lang.OngoingNode;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.molr.mole.core.runnable.lang.BranchMode.PARALLEL;
import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;
import static java.util.Objects.requireNonNull;

public class OngoingContextualBranch<C> extends OngoingNode<OngoingContextualBranch<C>> {

    private final Function<In, C> contextFactory;
    private BranchMode mode;
    private final AtomicBoolean asCalled = new AtomicBoolean(false);

    public OngoingContextualBranch(String name, RunnableLeafsMission.Builder builder, Block parent, BranchMode mode, Function<In, C> contextFactory) {
        super(
                requireNonNull(name, "branchName must not be null"), //
                requireNonNull(builder, "builder must not be null"), //
                parent /* parent may be null (special case for root branch)*/
        );
        this.mode = requireNonNull(mode, "mode must not be null");
        this.contextFactory = requireNonNull(contextFactory, "contextFactory must not be null.");
    }

    public OngoingContextualBranch<C> parallel() {
        this.mode = PARALLEL;
        return this;
    }

    public OngoingContextualBranch<C> sequential() {
        this.mode = SEQUENTIAL;
        return this;
    }

    public void as(Consumer<ContextualBranch<C>> branchDescription) {
        if (asCalled.getAndSet(true)) {
            throw new IllegalStateException("as() method must only be called once!");
        }
        requireNonNull(branchDescription, "branchDescription must not be null.");

        Block block = block();
        ContextualBranch<C> branch = new ContextualBranch<>(builder(), block, contextFactory);
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
