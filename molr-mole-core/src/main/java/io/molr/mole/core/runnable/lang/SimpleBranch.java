package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import java.util.function.Consumer;

import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;

public class SimpleBranch extends ForeachBranchProvidingAbstractBranch {

    protected SimpleBranch(RunnableLeafsMission.Builder builder, Block parent) {
        super(builder, parent);
    }

    static SimpleBranch withParent(RunnableLeafsMission.Builder builder, Block parent) {
        return new SimpleBranch(builder, parent);
    }

    @Override
    public OngoingSimpleBranch branch(String name, Placeholder<?>... placeholders) {
        return new OngoingSimpleBranch(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).build(), builder(), parent(), SEQUENTIAL);
    }

    @Override
    public OngoingSimpleLeaf leaf(String name, Placeholder<?>... placeholders) {
        return new OngoingSimpleLeaf(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).build(), builder(), parent());
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
