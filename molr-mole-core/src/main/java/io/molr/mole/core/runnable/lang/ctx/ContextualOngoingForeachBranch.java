package io.molr.mole.core.runnable.lang.ctx;

import java.util.function.BiConsumer;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.GenericOngoingBranch;

public class ContextualOngoingForeachBranch<C, T> extends GenericOngoingBranch<ContextualOngoingForeachBranch<C, T>>{
	
	Placeholder<T> itemPlaceholder;
	
	public ContextualOngoingForeachBranch(String name, Builder builder, Block parent, BranchMode mode, Placeholder<T> itemPlaceholder) {
		super(name, builder, parent, mode);
		this.itemPlaceholder = itemPlaceholder;
	}

	public void as(BiConsumer<ContextualForeachBranch<C, T>, Placeholder<T>> branchDescription) {
        Block block = block();
        ContextualForeachBranch<C, T> branch = new ContextualForeachBranch<>(builder(), block, itemPlaceholder);
        branchDescription.accept(branch, itemPlaceholder);	
	}
	
}
