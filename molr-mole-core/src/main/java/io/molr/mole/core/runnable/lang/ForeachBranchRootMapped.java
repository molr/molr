package io.molr.mole.core.runnable.lang;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.Placeholders;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;

public class ForeachBranchRootMapped<T, U> extends GenericOngoingBranch<ForeachBranchRootMapped<T, U>> {

	private Block block;
	Placeholder<T> itemPlaceholder;
	Placeholder<U> transformedItemPlaceholder;
	Placeholder<? extends Collection<T>> itemsPlaceholder;
	Function<In, U> function;

	@SuppressWarnings("unchecked")
	public ForeachBranchRootMapped(BlockNameConfiguration name, Builder builder, Block parent, BranchMode mode,
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
	
	public OngoingForeachBranch<U> branch(String name, Placeholder<?>... placeholders) {
		createAndAddForeachBlock();
		//builder().blockTextFormat(block, placeholders);
		for (int i = 0; i < placeholders.length; i++) {
			if(placeholders[i].equals(Placeholders.LATEST_FOREACH_ITEM_PLACEHOLDER)) {
				placeholders[i]=itemPlaceholder;
			}
		}
		BlockNameConfiguration blockNameConfig = BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build();
		return new OngoingForeachBranch<>(blockNameConfig, builder(), block, BranchMode.SEQUENTIAL, transformedItemPlaceholder);
	}
	
	public OngoingForeachLeaf<U> leaf(String name, Placeholder<?>... placeholders) {
		createAndAddForeachBlock();
		BlockNameConfiguration blockNameConfig = BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build();
		return new OngoingForeachLeaf<U>(blockNameConfig, builder(), block, transformedItemPlaceholder);
	}
	
}
