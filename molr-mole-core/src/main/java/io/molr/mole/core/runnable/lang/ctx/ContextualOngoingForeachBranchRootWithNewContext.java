package io.molr.mole.core.runnable.lang.ctx;

import java.util.UUID;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.GenericOngoingBranch;
import io.molr.mole.core.utils.Checkeds;

public class ContextualOngoingForeachBranchRootWithNewContext<C, T> extends GenericOngoingBranch<ContextualOngoingForeachBranchRootWithNewContext<C, T>>{
	
	Placeholder<T> itemPlaceholder;
	Function<In, C> contextFactory;
	
	public ContextualOngoingForeachBranchRootWithNewContext(String name, Builder builder, Block parent, BranchMode mode, Function<In, C> contextFactory, Placeholder<T> itemPlaceholder) {
		super(name, builder, parent, mode);
		this.contextFactory = contextFactory;
		this.itemPlaceholder = itemPlaceholder;
	}

	public void as(Checkeds.CheckedThrowingConsumer3<ContextualForeachBranch<C, T>, Placeholder<C>, Placeholder<T>> branchDescription) {
		
        Block block = block();
        
        @SuppressWarnings("unchecked")
		Placeholder<C> newContextPlaceholder = (Placeholder<C>) Placeholder.of(Object.class, UUID.randomUUID().toString());
        builder().contextFactory(block, newContextPlaceholder, contextFactory);
        
        ContextualForeachBranch<C, T> branch = new ContextualForeachBranch<>(builder(), block, newContextPlaceholder, itemPlaceholder);
        branchDescription.accept(branch, newContextPlaceholder, itemPlaceholder);	
	}
	
}
