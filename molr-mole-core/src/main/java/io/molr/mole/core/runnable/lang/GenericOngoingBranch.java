package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MolrCollection;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static io.molr.mole.core.runnable.lang.BranchMode.PARALLEL;
import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;
import static java.util.Objects.requireNonNull;

public abstract class GenericOngoingBranch<B extends GenericOngoingBranch<B>> extends OngoingNode<B> {

    private BranchMode mode;


    public GenericOngoingBranch(String name, RunnableLeafsMission.Builder builder, Block parent, BranchMode mode) {
        super(
                requireNonNull(name, "branchName must not be null"), //
                requireNonNull(builder, "builder must not be null"), //
                parent /* parent may be null (special case for root branch)*/
        );
        this.mode = requireNonNull(mode);
    }

    public B parallel() {
        this.mode = PARALLEL;
        return (B) this;
    }

    public B sequential() {
        this.mode = SEQUENTIAL;
        return (B) this;
    }
    
    public <T> B foreach(Placeholder<? extends MolrCollection<T>> collectionPlaceholder) {
    	Placeholder<T> item = MolrCollection.itemPlaceholderForCollectionPlaceholder(collectionPlaceholder, "test");
    	
    	return (B) this;
    }


    protected Block block() {
        if (parent() == null) {
            return builder().rootBranchNode(name(), mode, blockAttributes());
        } else {
            return builder().childBranchNode(parent(), name(), mode, blockAttributes());
        }
    }

    protected BranchMode mode() {
        return this.mode;
    }

}
