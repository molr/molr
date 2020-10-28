package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MolrCollection;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

import java.util.Random;
import java.util.UUID;

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
    
    public <T> Placeholder<T> forEach(Placeholder<? extends MolrCollection<T>> itemsPlaceholder, BiConsumer<SimpleBranch, Placeholder<T>> branchDescription) {
        if (asCalled.getAndSet(true)) {
            throw new IllegalStateException("as() method must only be called once!");
        }
        requireNonNull(branchDescription, "branchDescription must not be null.");

        Placeholder<T> itemPlaceholder = MolrCollection.itemPlaceholderForCollectionPlaceholder(itemsPlaceholder, UUID.randomUUID().toString());
        System.out.println(itemPlaceholder);


        Block block = block();
        builder().forEachBlock(block, itemsPlaceholder, itemPlaceholder);
        SimpleBranch branch = SimpleBranch.withParent(builder(), block);
        branchDescription.accept(branch, itemPlaceholder);
        
        return itemPlaceholder;
    }
    
    public <T> ForeachBranchRoot<T> foreachItem(Placeholder<? extends MolrCollection<T>> itemsPlaceholder) {
        if (asCalled.getAndSet(true)) {
            throw new IllegalStateException("as() method must only be called once!");
        }
        return new ForeachBranchRoot<>(name(), builder(), parent(), mode(), itemsPlaceholder);
    }


}
