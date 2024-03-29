package io.molr.mole.core.runnable.lang;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MolrCollection;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;

public class OngoingRootBranch extends OngoingContextualOptionProvidingBranch<OngoingRootBranch> {

    private final AtomicBoolean asCalled = new AtomicBoolean(false);

    public OngoingRootBranch(BlockNameConfiguration name, RunnableLeafsMission.Builder builder, Block parent,
            BranchMode mode) {
        super(name, builder, parent, mode, Map.of());
    }

    public void as(Consumer<SimpleBranch> branchDescription) {
        /* let is ignored when branch is modified */
        if (asCalled.getAndSet(true)) {
            throw new IllegalStateException("as() method must only be called once!");
        }
        requireNonNull(branchDescription, "branchDescription must not be null.");

        Block block = block();
        SimpleBranch branch = SimpleBranch.withParent(builder(), block);
        branchDescription.accept(branch);

    }

    public <T> ForeachBranchRoot<T> foreach(Placeholder<? extends MolrCollection<T>> itemsPlaceholder) {
        String name = "forEachItemIn:" + itemsPlaceholder.name();
        Block block = block();
        return new ForeachBranchRoot<>(BlockNameConfiguration.builder().text(name).build(), builder(), block,
                BranchMode.sequential(), itemsPlaceholder, getMappings());
    }

    public <T> ForeachBranchRoot<T> foreach(Placeholder<T> itemPlaceholder,
            Placeholder<? extends MolrCollection<T>> itemsPlaceholder) {
        String name = "forEachItemIn:" + itemsPlaceholder.name();
        Block block = block();

        return new ForeachBranchRoot<>(BlockNameConfiguration.builder().text(name).build(), builder(), block,
                BranchMode.sequential(), itemPlaceholder, itemsPlaceholder, getMappings());
    }

}
