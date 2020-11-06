package io.molr.mole.core.runnable.lang;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;

public class ForeachBranchRootMapped<T, U> extends GenericOngoingBranch<ForeachBranchRootMapped<T, U>> {

	private Block block;
	Placeholder<T> itemPlaceholder;
	Placeholder<U> transformedItemPlaceholder;
	Placeholder<? extends Collection<T>> itemsPlaceholder;
	Function<In, U> function;

	@SuppressWarnings("unchecked")
	public ForeachBranchRootMapped(String name, Builder builder, Block parent, BranchMode mode,
			Placeholder<? extends Collection<T>> itemsPlaceholder, Placeholder<T> itemPlaceholder, Function<In, U> function) {
		super(name, builder, parent, mode);

		this.itemsPlaceholder = itemsPlaceholder;
		this.itemPlaceholder = itemPlaceholder;
		this.transformedItemPlaceholder = (Placeholder<U>) Placeholder.of(Object.class, UUID.randomUUID().toString());
		this.function = function;
	}

	private void createAndAddForeachBlock() {
		this.block = block();
		builder().forEachBlock(block, itemsPlaceholder, itemPlaceholder, transformedItemPlaceholder, function);
	}
	
	public OngoingForeachBranch<U> branch(String name) {
		createAndAddForeachBlock();
		return new OngoingForeachBranch<>(name, builder(), block, BranchMode.SEQUENTIAL, transformedItemPlaceholder);
	}

	public OngoingForeachLeaf<U> leaf(String name) {
		createAndAddForeachBlock();
		return new OngoingForeachLeaf<U>(name, builder(), block, transformedItemPlaceholder);
	}
	
}
