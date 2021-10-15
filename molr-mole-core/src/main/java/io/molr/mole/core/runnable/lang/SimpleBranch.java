package io.molr.mole.core.runnable.lang;

import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;

import java.util.Map;
import java.util.function.Consumer;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;

public class SimpleBranch extends ForeachBranchProvidingAbstractBranch {

    protected SimpleBranch(RunnableLeafsMission.Builder builder, Block parent) {
        super(builder, parent);
    }

    static SimpleBranch withParent(RunnableLeafsMission.Builder builder, Block parent) {
        return new SimpleBranch(builder, parent);
    }

    @Override
    public OngoingSimpleBranch branch(String name) {
        return branch(name, new Placeholder<?>[] {});
    }

    @Override
    public OngoingSimpleLeaf leaf(String name) {
        return leaf(name, new Placeholder<?>[] {});
    }

    @Override
    public OngoingSimpleBranch branch(String name, Placeholder<?>... placeholders) {
        return new OngoingSimpleBranch(
                BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).build(), builder(),
                parent(), SEQUENTIAL, Map.of());
    }

    @Override
    public OngoingSimpleLeaf leaf(String name, Placeholder<?>... placeholders) {
        return new OngoingSimpleLeaf(
                BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).build(), builder(),
                parent(), Map.of());
    }

    @Deprecated
    public void sequential(String name, Consumer<SimpleBranch> branchDefiner) {
        this.branch(name).sequential().as(branchDefiner);
    }

    @Deprecated
    public void parallel(String name, Consumer<SimpleBranch> branchDefiner) {
        branch(name).parallel().as(branchDefiner);
    }

}
