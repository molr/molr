package io.molr.mole.core.runnable.lang.ctx;

import static java.util.Objects.requireNonNull;

import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.BlockNameConfiguration;

public class ContextualBranch<C> extends ContextualForeachBranchProvidingAbstractBranch<C> {

	Placeholder<C> contextPlaceholder;
	
    protected ContextualBranch(RunnableLeafsMission.Builder builder, Block parent, Placeholder<C> contextPlaceholder) {
        super(builder, parent, contextPlaceholder);
        requireNonNull(contextPlaceholder);
        this.contextPlaceholder = contextPlaceholder;
    }

    @Override
    public OngoingContextualBranch<C> branch(String name, Placeholder<?>... placeholders) {
        return new OngoingContextualBranch<>(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).build(), builder(), parent(), SEQUENTIAL, contextPlaceholder);
    }

    @Override
    public OngoingContextualLeaf<C> leaf(String name, Placeholder<?>... placeholders) {
        return new OngoingContextualLeaf<>(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).build(), builder(), parent(), contextPlaceholder);
    }

}
