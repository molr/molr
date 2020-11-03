package io.molr.mole.core.runnable.lang.ctx;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.MolrCollection;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.GenericOngoingBranch;

public class ContextualForeachBranchRoot<C, T> extends GenericOngoingBranch<ContextualForeachBranchRoot<C, T>> {

	private Block block;
	Placeholder<T> itemPlaceholder;
	Placeholder<C> contextPlaceholder;
	Placeholder<? extends MolrCollection<T>> itemsPlaceholder;

	public ContextualForeachBranchRoot(String name, Builder builder, Block parent, BranchMode mode, Placeholder<C> contextPlaceholder,
			Placeholder<? extends MolrCollection<T>> itemsPlaceholder) {
		super(name, builder, parent, mode);

		this.contextPlaceholder = contextPlaceholder;
		this.itemsPlaceholder = itemsPlaceholder;
		this.itemPlaceholder = MolrCollection.itemPlaceholderForCollectionPlaceholder(itemsPlaceholder,
				UUID.randomUUID().toString());
	}

	private void createAndAddForeachBlock() {
		this.block = block();
		builder().forEachBlock(block, itemsPlaceholder, itemPlaceholder);
	}
	
	public ContextualOngoingForeachBranchRoot<C, T> branch(String name) {
		createAndAddForeachBlock();
		return new ContextualOngoingForeachBranchRoot<>(name, builder(), block, BranchMode.SEQUENTIAL, contextPlaceholder, itemPlaceholder);
	}

	public ContextualOngoingForeachLeaf<C, T> leaf(String name) {
		createAndAddForeachBlock();
		return new ContextualOngoingForeachLeaf<C, T>(name, builder(), block, contextPlaceholder, itemPlaceholder);
	}
	
    public <B> ContextualOngoingForeachBranchRootWithNewContext<B, T> contextual(Function<In, B> contextFactory) {
        return new ContextualOngoingForeachBranchRootWithNewContext<>(name(), builder(), parent(), mode(), contextFactory, itemPlaceholder);
    }

    public <B, P1> ContextualOngoingForeachBranchRootWithNewContext<B, T> contextual(Function<P1, B> contextFactory, Placeholder<P1> p1) {
        return contextual(in -> contextFactory.apply(in.get(p1)));
    }

    public <B, P1, P2> ContextualOngoingForeachBranchRootWithNewContext<B, T> contextual(BiFunction<P1, P2, B> contextFactory, Placeholder<P1> p1, Placeholder<P2> p2) {
        return contextual(in -> contextFactory.apply(in.get(p1), in.get(p2)));
    }

}
