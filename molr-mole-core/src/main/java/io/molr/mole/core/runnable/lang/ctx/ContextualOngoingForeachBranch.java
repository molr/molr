package io.molr.mole.core.runnable.lang.ctx;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.GenericOngoingBranch;
import io.molr.mole.core.runnable.lang.OngoingRootBranch;

public class ContextualOngoingForeachBranch<C, T> extends GenericOngoingBranch<ContextualOngoingForeachBranch<C, T>>{
	
	Placeholder<T> itemPlaceholder;
	Placeholder<C> contextPlaceholder;
	
	public ContextualOngoingForeachBranch(String name, Builder builder, Block parent, BranchMode mode, Placeholder<C> contextPlaceholder, Placeholder<T> itemPlaceholder) {
		super(name, builder, parent, mode);
		this.contextPlaceholder = contextPlaceholder;
		this.itemPlaceholder = itemPlaceholder;
	}

	public void as(BiConsumer<ContextualForeachBranch<C, T>, Placeholder<T>> branchDescription) {
        Block block = block();
        ContextualForeachBranch<C, T> branch = new ContextualForeachBranch<>(builder(), block, contextPlaceholder, itemPlaceholder);
        branchDescription.accept(branch, itemPlaceholder);	
	}

    public <B> OngoingContextualBranch<B> contextual(Function<In, B> contextFactory) {
//        builder().contextFactory(contextFactory);
    	
        @SuppressWarnings("unchecked")
		Placeholder<B> newContextPlaceholder = (Placeholder<B>) Placeholder.of(Object.class, UUID.randomUUID().toString());
        builder().contextFactory(newContextPlaceholder, contextFactory);

        return new OngoingContextualBranch<>(name(), builder(), parent(), mode(), newContextPlaceholder, true);
    }

    public <B, P1> OngoingContextualBranch<B> contextual(Function<P1, B> contextFactory, Placeholder<P1> p1) {
        return contextual(in -> contextFactory.apply(in.get(p1)));
    }

    public <B, P1, P2> OngoingContextualBranch<B> contextual(BiFunction<P1, P2, B> contextFactory, Placeholder<P1> p1, Placeholder<P2> p2) {
        return contextual(in -> contextFactory.apply(in.get(p1), in.get(p2)));
    }
	
}
