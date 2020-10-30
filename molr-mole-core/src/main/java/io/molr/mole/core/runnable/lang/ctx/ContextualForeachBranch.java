package io.molr.mole.core.runnable.lang.ctx;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.BranchMode;

public class ContextualForeachBranch<C, T> extends ContextualForeachBranchProvidingAbstractBranch<C>{

	Placeholder<T> itemPlaceholder;
	Placeholder<C> contextPlaceholder;
	
	protected ContextualForeachBranch(Builder builder, Block parent, Placeholder<C> contextPlaceholder, Placeholder<T> itemPlaceholder) {
		super(builder, parent, contextPlaceholder);
		this.contextPlaceholder = contextPlaceholder;
		this.itemPlaceholder = itemPlaceholder;
	}

	@Override
	public ContextualOngoingForeachBranch<C, T> branch(String name) {
		return new ContextualOngoingForeachBranch<>(name, builder(), parent(), BranchMode.SEQUENTIAL, contextPlaceholder, itemPlaceholder);
	}

	@Override
	public ContextualOngoingForeachLeaf<C, T> leaf(String name) {
		return new ContextualOngoingForeachLeaf<>(name, builder(), parent(), contextPlaceholder, itemPlaceholder);
	}
}
