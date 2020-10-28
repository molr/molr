package io.molr.mole.core.runnable.lang;

import java.util.function.BiConsumer;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;

public class OngoingForeachBranch<T> extends GenericOngoingBranch<OngoingForeachBranch<T>>{
	
	Placeholder<T> itemPlaceholder;
	
	public OngoingForeachBranch(String name, Builder builder, Block parent, BranchMode mode, Placeholder<T> itemPlaceholder) {
		super(name, builder, parent, mode);
		this.itemPlaceholder = itemPlaceholder;
	}

	public void as(BiConsumer<ForeachBranch<T>, Placeholder<T>> branchDescription) {
        Block block = block();
        ForeachBranch<T> branch = new ForeachBranch<T>(builder(), block, itemPlaceholder);
        branchDescription.accept(branch, itemPlaceholder);	
	}
	
}
