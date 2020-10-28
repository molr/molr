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
	public OngoingForeachBranch<T> branch(String name) {
		return new OngoingForeachBranch<T>(name, builder(), parent(), BranchMode.SEQUENTIAL, itemPlaceholder);
	}

	@Override
	public OngoingForeachLeaf<T> leaf(String name) {
		return new OngoingForeachLeaf<>(name, builder(), parent(), itemPlaceholder);
	}
}
