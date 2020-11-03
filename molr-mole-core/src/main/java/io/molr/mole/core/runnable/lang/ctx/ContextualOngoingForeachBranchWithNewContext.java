package io.molr.mole.core.runnable.lang.ctx;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.GenericOngoingBranch;

public class ContextualOngoingForeachBranchWithNewContext<C, T> extends GenericOngoingBranch<ContextualOngoingForeachBranchWithNewContext<C, T>>{
	
	Placeholder<T> itemPlaceholder;
	Function<In,C> contextFactory;
	
	
	public ContextualOngoingForeachBranchWithNewContext(String name, Builder builder, Block parent, BranchMode mode, Function<In,C> contextFactory, Placeholder<T> itemPlaceholder) {
		super(name, builder, parent, mode);
		this.contextFactory = contextFactory;
		this.itemPlaceholder = itemPlaceholder;
	}

	public void as(BiConsumer<ContextualForeachBranch<C, T>, Placeholder<C>> branchDescription) {
        Block block = block();
        
        @SuppressWarnings("unchecked")
		Placeholder<C> newContextPlaceholder = (Placeholder<C>) Placeholder.of(Object.class, UUID.randomUUID().toString());
        builder().contextFactory(block, newContextPlaceholder, contextFactory);
        
        ContextualForeachBranch<C, T> branch = new ContextualForeachBranch<>(builder(), block, newContextPlaceholder, itemPlaceholder);
        branchDescription.accept(branch, newContextPlaceholder);	
	}
	
}
