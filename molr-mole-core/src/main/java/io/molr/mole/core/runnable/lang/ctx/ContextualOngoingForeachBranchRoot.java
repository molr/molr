package io.molr.mole.core.runnable.lang.ctx;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.GenericOngoingBranch;

public class ContextualOngoingForeachBranchRoot<C, T> extends GenericOngoingBranch<ContextualOngoingForeachBranchRoot<C, T>>{
	
	Placeholder<T> itemPlaceholder;
	Placeholder<C> contextPlaceholder;
	
	public ContextualOngoingForeachBranchRoot(String name, Builder builder, Block parent, BranchMode mode, Placeholder<C> contextPlaceholder, Placeholder<T> itemPlaceholder) {
		super(name, builder, parent, mode);
		this.contextPlaceholder = contextPlaceholder;
		this.itemPlaceholder = itemPlaceholder;
	}

	public void as(BiConsumer<ContextualForeachBranch<C, T>, Placeholder<T>> branchDescription) {
        Block block = block();
        ContextualForeachBranch<C, T> branch = new ContextualForeachBranch<>(builder(), block, contextPlaceholder, itemPlaceholder);
        branchDescription.accept(branch, itemPlaceholder);	
	}

    public <B> ContextualOngoingForeachBranchRootWithNewContext<B, T> contextual(Function<In, B> contextFactory) {
//        builder().contextFactory(contextFactory);
        //return new ContextualOngoingForeachBranch<>(name(), builder(), parent(), mode(), newContextPlaceholder, true);
        return new ContextualOngoingForeachBranchRootWithNewContext<>(name(), builder(), parent(), mode(), contextFactory, itemPlaceholder);
    }


    public <B> ContextualOngoingForeachBranchRootWithNewContext<B, T> contextualFor(Function<T, B> contextFactory) {
        return contextual(in -> contextFactory.apply(in.get(itemPlaceholder)));
    }
    
    public <B, P1> ContextualOngoingForeachBranchRootWithNewContext<B, T> contextual(Function<P1, B> contextFactory, Placeholder<P1> p1) {
        return contextual(in -> contextFactory.apply(in.get(p1)));
    }

    public <B, P1, P2> ContextualOngoingForeachBranchRootWithNewContext<B, T> contextual(BiFunction<P1, P2, B> contextFactory, Placeholder<P1> p1, Placeholder<P2> p2) {
        return contextual(in -> contextFactory.apply(in.get(p1), in.get(p2)));
    }
	
}
