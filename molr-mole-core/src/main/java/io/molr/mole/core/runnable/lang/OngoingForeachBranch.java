package io.molr.mole.core.runnable.lang;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.ctx.ContextualOngoingForeachBranchWithNewContext;

public class OngoingForeachBranch<T> extends GenericOngoingBranch<OngoingForeachBranch<T>>{
	
	Placeholder<T> itemPlaceholder;
	
	public OngoingForeachBranch(BlockNameConfiguration name, Builder builder, Block parent, BranchMode mode, Placeholder<T> itemPlaceholder) {
		super(name, builder, parent, mode);
		this.itemPlaceholder = itemPlaceholder;
	}

	public void as(BiConsumer<ForeachBranch<T>, Placeholder<T>> branchDescription) {
        Block block = block();
        ForeachBranch<T> branch = new ForeachBranch<T>(builder(), block, itemPlaceholder);
        branchDescription.accept(branch, itemPlaceholder);	
	}
	
    public <C> ContextualOngoingForeachBranchWithNewContext<C, T> contextual(Function<In, C> contextFactory) {
        return new ContextualOngoingForeachBranchWithNewContext<>(name(), builder(), parent(), mode(), contextFactory, itemPlaceholder);
    }
    
    public <C, P1> ContextualOngoingForeachBranchWithNewContext<C, T> contextualFor(Function<T, C> contextFactory) {
        return contextual(in -> contextFactory.apply(in.get(itemPlaceholder)));
    }

    public <C, P1> ContextualOngoingForeachBranchWithNewContext<C, T> contextual(Function<P1, C> contextFactory, Placeholder<P1> p1) {
        return contextual(in -> contextFactory.apply(in.get(p1)));
    }

    public <C, P1, P2> ContextualOngoingForeachBranchWithNewContext<C, T> contextual(BiFunction<P1, P2, C> contextFactory, Placeholder<P1> p1, Placeholder<P2> p2) {
        return contextual(in -> contextFactory.apply(in.get(p1), in.get(p2)));
    }
	
}
