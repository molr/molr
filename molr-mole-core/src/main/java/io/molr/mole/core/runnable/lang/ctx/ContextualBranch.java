package io.molr.mole.core.runnable.lang.ctx;

import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;

public class ContextualBranch<C> extends ContextualForeachBranchProvidingAbstractBranch<C> {

	Placeholder<C> contextPlaceholder;
	
    protected ContextualBranch(RunnableLeafsMission.Builder builder, Block parent, Placeholder<C> contextPlaceholder) {
        super(builder, parent, contextPlaceholder);
        this.contextPlaceholder = contextPlaceholder;
    }

    @Override
    public OngoingContextualBranch<C> branch(String name) {
        return new OngoingContextualBranch<>(name, builder(), parent(), SEQUENTIAL, contextPlaceholder);
    }

    @Override
    public OngoingContextualLeaf<C> leaf(String name) {
        return new OngoingContextualLeaf<>(name, builder(), parent(), contextPlaceholder);
    }

}
