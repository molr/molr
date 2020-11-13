package io.molr.mole.core.runnable.lang.ctx;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import java.util.Collection;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.GenericOngoingBranch;
import io.molr.mole.core.runnable.lang.BlockNameConfiguration;

public class ContextualForeachBranchRoot<C, T> extends GenericOngoingBranch<ContextualForeachBranchRoot<C, T>> {

	private Block block;
	Placeholder<T> itemPlaceholder;
	Placeholder<C> contextPlaceholder;
	Placeholder<? extends Collection<T>> itemsPlaceholder;

	@SuppressWarnings("unchecked")
	public ContextualForeachBranchRoot(BlockNameConfiguration name, Builder builder, Block parent, BranchMode mode, Placeholder<C> contextPlaceholder,
			Placeholder<? extends Collection<T>> itemsPlaceholder) {
		super(name, builder, parent, mode);

		this.contextPlaceholder = contextPlaceholder;
		this.itemsPlaceholder = itemsPlaceholder;
		this.itemPlaceholder = (Placeholder<T>) Placeholder.of(Object.class, UUID.randomUUID().toString());
	}

	private void createAndAddForeachBlock() {
		this.block = block();
		builder().forEachBlock(block, itemsPlaceholder, itemPlaceholder);
	}
	
	public ContextualOngoingForeachBranchRoot<C, T> branch(String name, Placeholder<?>... placeholders) {
		createAndAddForeachBlock();
		return new ContextualOngoingForeachBranchRoot<>(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build(), builder(), block, BranchMode.SEQUENTIAL, contextPlaceholder, itemPlaceholder);
	}

	public ContextualOngoingForeachLeaf<C, T> leaf(String name, Placeholder<?>... placeholders) {
		createAndAddForeachBlock();
		return new ContextualOngoingForeachLeaf<C, T>(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build(), builder(), block, contextPlaceholder, itemPlaceholder);
	}

    public <U> ContextualForeachBranchRootMapped<C, T, U> map(Function<T, U> function) {
        return mapIt(in -> function.apply(in.get(itemPlaceholder)));
    }

    public <U, P1> ContextualForeachBranchRootMapped<C, T, U> map(BiFunction<T, P1, U> contextFactory, Placeholder<P1> p1) {
        return mapIt(in -> contextFactory.apply(in.get(itemPlaceholder), in.get(p1)));
    }
    
    private <U> ContextualForeachBranchRootMapped<C, T, U> mapIt(Function<In, U> function) {
        return new ContextualForeachBranchRootMapped<C, T, U>(name(), builder(), parent(), mode(), contextPlaceholder, itemsPlaceholder, itemPlaceholder, function);
    }
	
}
