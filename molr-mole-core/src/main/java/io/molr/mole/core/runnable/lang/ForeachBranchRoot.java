package io.molr.mole.core.runnable.lang;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.MolrCollection;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.ctx.ContextualOngoingForeachBranchRootWithNewContext;

public class ForeachBranchRoot<T> extends GenericOngoingBranch<ForeachBranchRoot<T>> {

	private Block block;
	Placeholder<T> itemPlaceholder;
	Placeholder<? extends MolrCollection<T>> itemsPlaceholder;

	public ForeachBranchRoot(String name, Builder builder, Block parent, BranchMode mode,
			Placeholder<? extends MolrCollection<T>> itemsPlaceholder) {
		super(name, builder, parent, mode);

		this.itemsPlaceholder = itemsPlaceholder;
		this.itemPlaceholder = MolrCollection.itemPlaceholderForCollectionPlaceholder(itemsPlaceholder,
				UUID.randomUUID().toString());
	}

	private void createAndAddForeachBlock() {
		this.block = block();
		builder().forEachBlock(block, itemsPlaceholder, itemPlaceholder);
	}
	
	public OngoingForeachBranch<T> branch(String name) {
		createAndAddForeachBlock();
		return new OngoingForeachBranch<>(name, builder(), block, BranchMode.SEQUENTIAL, itemPlaceholder);
	}

	public OngoingForeachLeaf<T> leaf(String name) {
		createAndAddForeachBlock();
		return new OngoingForeachLeaf<T>(name, builder(), block, itemPlaceholder);
	}
	
    public <C> ContextualOngoingForeachBranchRootWithNewContext<C, T> contextual(Function<In, C> contextFactory) {
        return new ContextualOngoingForeachBranchRootWithNewContext<>(name(), builder(), parent(), mode(), contextFactory, itemPlaceholder);
    }

    public <C, P1> ContextualOngoingForeachBranchRootWithNewContext<C, T> contextual(Function<P1, C> contextFactory, Placeholder<P1> p1) {
        return contextual(in -> contextFactory.apply(in.get(p1)));
    }

    public <C, P1, P2> ContextualOngoingForeachBranchRootWithNewContext<C, T> contextual(BiFunction<P1, P2, C> contextFactory, Placeholder<P1> p1, Placeholder<P2> p2) {
        return contextual(in -> contextFactory.apply(in.get(p1), in.get(p2)));
    }
	

}
