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

public class ContextualForeachBranchRootMapped<C, T, U> extends GenericOngoingBranch<ContextualForeachBranchRootMapped<C, T, U>>{

	private Placeholder<C> contextPlaceholder;
	private Placeholder<T> itemPlaceholder;
	private Placeholder<? extends Collection<T>> itemsPlaceholder;
	private Placeholder<U> transformedItemPlaceholder;
	private Function<In, U> function;
	
	@SuppressWarnings("unchecked")
	public ContextualForeachBranchRootMapped(BlockNameConfiguration name, Builder builder, Block parent, BranchMode mode,
			Placeholder<C> contextPlaceholder, Placeholder<? extends Collection<T>> itemsPlaceholder, Placeholder<T> itemPlaceholder,
			Function<In, U> function) {
		super(name, builder, parent, mode);
		this.contextPlaceholder = contextPlaceholder;
		this.itemPlaceholder = itemPlaceholder;
		this.itemsPlaceholder = itemsPlaceholder;
		this.transformedItemPlaceholder = (Placeholder<U>)Placeholder.of(Object.class, UUID.randomUUID().toString());
		this.function = function;
	}
	
	private Block createAndAddForeachBlock() {
		Block block = block();
		builder().forEachBlock(block, itemsPlaceholder, itemPlaceholder, transformedItemPlaceholder, function);
		return block;
	}
	
	public ContextualOngoingForeachBranchRoot<C, U> branch(String name, Placeholder<?>... placeholders) {
		Block block = createAndAddForeachBlock();
		return new ContextualOngoingForeachBranchRoot<>(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build(), builder(), block, BranchMode.SEQUENTIAL, contextPlaceholder, transformedItemPlaceholder);
	}

	public ContextualOngoingForeachLeaf<C, U> leaf(String name, Placeholder<?>... placeholders) {
		Block block = createAndAddForeachBlock();
		return new ContextualOngoingForeachLeaf<C, U>(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build(), builder(), block, contextPlaceholder, transformedItemPlaceholder);
	}	

}
