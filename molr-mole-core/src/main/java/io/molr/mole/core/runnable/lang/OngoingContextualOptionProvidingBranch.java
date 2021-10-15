package io.molr.mole.core.runnable.lang;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.ctx.OngoingContextualBranchWithNewContext;

public class OngoingContextualOptionProvidingBranch<A extends OngoingContextualOptionProvidingBranch<A>> extends GenericOngoingBranch<A>{

	public OngoingContextualOptionProvidingBranch(BlockNameConfiguration name, Builder builder, Block parent,
			BranchMode mode, Map<Placeholder<?>, Function<In, ?>> mappings) {
		super(name, builder, parent, mode, mappings);
	}
	
    public <C> OngoingContextualBranchWithNewContext<C> contextual(Supplier<C> contextFactory) {
        return contextual(in -> contextFactory.get());
    }
	
    public <C> OngoingContextualBranchWithNewContext<C> contextual(Function<In, C> contextFactory) {
        return new OngoingContextualBranchWithNewContext<>(name(), builder(), parent(), mode(), contextFactory, getMappings());
    }

    public <C, P1> OngoingContextualBranchWithNewContext<C> contextual(Function<P1, C> contextFactory, Placeholder<P1> p1) {
        return contextual(in -> contextFactory.apply(in.get(p1)));
    }

    public <C, P1, P2> OngoingContextualBranchWithNewContext<C> contextual(BiFunction<P1, P2, C> contextFactory, Placeholder<P1> p1, Placeholder<P2> p2) {
        return contextual(in -> contextFactory.apply(in.get(p1), in.get(p2)));
    }

}
