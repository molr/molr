package io.molr.mole.core.runnable.lang.ctx;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.BlockNameConfiguration;
import io.molr.mole.core.runnable.lang.ForeachBranchProvidingAbstractBranch;
import io.molr.mole.core.runnable.lang.OngoingForeachLeaf;

public class ContextualBranch<C> extends ForeachBranchProvidingAbstractBranch {
	Placeholder<C> contextPlaceholder;
	
    protected ContextualBranch(RunnableLeafsMission.Builder builder, Block parent, Placeholder<C> contextPlaceholder) {
        super(builder, parent);
        requireNonNull(contextPlaceholder);
        this.contextPlaceholder = contextPlaceholder;
    }

    @Override
    public OngoingContextualBranch<C> branch(String name, Placeholder<?>... placeholders) {
        return new OngoingContextualBranch<>(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).build(), builder(), parent(), SEQUENTIAL, contextPlaceholder, Map.of());
    }

    @Override
    public OngoingForeachLeaf<C> leaf(String name, Placeholder<?>... placeholders) {
        return new OngoingForeachLeaf<>(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).build(), builder(), parent(), contextPlaceholder, Map.of());
    }

}
