package io.molr.mole.core.runnable.lang;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Map;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;

public abstract class ForeachBranchProvidingAbstractBranch extends AbstractBranch {

    protected ForeachBranchProvidingAbstractBranch(Builder builder, Block parent) {
        super(builder, parent);

    }

    public <T> ForeachBranchRoot<T> foreach(Placeholder<? extends Collection<T>> itemsPlaceholder) {
        requireNonNull(itemsPlaceholder);
        String name = "forEachItemIn:" + itemsPlaceholder.name();
        BlockNameConfiguration formatter = BlockNameConfiguration.builder().text(name).build();
        return new ForeachBranchRoot<>(formatter, builder(), parent(), BranchMode.SEQUENTIAL, itemsPlaceholder,
                Map.of());
    }

}
