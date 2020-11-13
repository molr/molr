package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;

public class ForeachBranch<T> extends ForeachBranchProvidingAbstractBranch{

	Placeholder<T> itemPlaceholder;
	
	protected ForeachBranch(Builder builder, Block parent, Placeholder<T> itemPlaceholder) {
		super(builder, parent);
		this.itemPlaceholder = itemPlaceholder;
	}

	@Override
	public OngoingForeachBranch<T> branch(String name, Placeholder<?>... placeholders) {
		BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build();
		return new OngoingForeachBranch<T>(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build(),
				builder(), parent(), BranchMode.SEQUENTIAL, itemPlaceholder);
	}

	@Override
	public OngoingForeachLeaf<T> leaf(String name, Placeholder<?>... placeholders) {
		return new OngoingForeachLeaf<>(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build(),
				builder(), parent(), itemPlaceholder);
	}
}
