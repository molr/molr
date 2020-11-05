package io.molr.mole.core.runnable.lang;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.MolrCollection;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;

public class ForeachBranchRoot<T> extends GenericOngoingBranch<ForeachBranchRoot<T>> {

	private Block block;
	Placeholder<T> itemPlaceholder;
	Placeholder<? extends MolrCollection<T>> itemsPlaceholder;

	@SuppressWarnings("unchecked")
	public ForeachBranchRoot(String name, Builder builder, Block parent, BranchMode mode,
			Placeholder<? extends MolrCollection<T>> itemsPlaceholder) {
		super(name, builder, parent, mode);

		this.itemsPlaceholder = itemsPlaceholder;
		this.itemPlaceholder = (Placeholder<T>) Placeholder.of(Object.class, UUID.randomUUID().toString());
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

    public <C> ForeachBranchRootMapped<T, C> map(Function<T, C> contextFactory) {
        return mapIt(in -> contextFactory.apply(in.get(itemPlaceholder)));
    }

    public <C, P1> ForeachBranchRootMapped<T, C> map(BiFunction<T, P1, C> contextFactory, Placeholder<P1> p1) {
        return mapIt(in -> contextFactory.apply(in.get(itemPlaceholder), in.get(p1)));
    }
    
    private <C> ForeachBranchRootMapped<T, C> mapIt(Function<In, C> contextFactory) {
        return new ForeachBranchRootMapped<T, C>(name(), builder(), parent(), mode(), itemsPlaceholder, itemPlaceholder, contextFactory);
    }
}
