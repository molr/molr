package io.molr.mole.core.runnable.lang.ctx;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.BlockNameConfiguration;
import static java.util.Objects.requireNonNull;

public class ContextualForeachBranch<C, T> extends ContextualForeachBranchProvidingAbstractBranch<C>{

	Placeholder<T> itemPlaceholder;
	Placeholder<C> contextPlaceholder;
	
	protected ContextualForeachBranch(Builder builder, Block parent, Placeholder<C> contextPlaceholder, Placeholder<T> itemPlaceholder) {
		super(builder, parent, contextPlaceholder);
		requireNonNull(contextPlaceholder);
		this.contextPlaceholder = contextPlaceholder;
		requireNonNull(itemPlaceholder);
		this.itemPlaceholder = itemPlaceholder;
	}

	@Override
	public ContextualOngoingForeachBranch<C, T> branch(String name, Placeholder<?>...placeholders) {
		return new ContextualOngoingForeachBranch<>(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build(), builder(), parent(), BranchMode.SEQUENTIAL, contextPlaceholder, itemPlaceholder);
	}

	@Override
	public ContextualOngoingForeachLeaf<C, T> leaf(String name, Placeholder<?>...placeholders) {
		return new ContextualOngoingForeachLeaf<>(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build(), builder(), parent(), contextPlaceholder, itemPlaceholder);
	}
}
