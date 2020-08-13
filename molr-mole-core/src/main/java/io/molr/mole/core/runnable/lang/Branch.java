package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.mole.core.runnable.RunnableLeafsMission;

import java.util.function.Consumer;

import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;

public class Branch extends AbstractBranch {

    protected Branch(RunnableLeafsMission.Builder builder, Block parent) {
        super(builder, parent);
    }

    static Branch withParent(RunnableLeafsMission.Builder builder, Block parent) {
        return new Branch(builder, parent);
    }

    public OngoingSimpleBranch branch(String name) {
        return new OngoingSimpleBranch(name, builder(), parent(), SEQUENTIAL);
    }

    @Deprecated
    public void sequential(String name, Consumer<Branch> branchDefiner) {
        this.branch(name).sequential().as(branchDefiner);
    }

    @Deprecated
    public void parallel(String name, Consumer<Branch> branchDefiner) {
        branch(name).parallel().as(branchDefiner);
    }


}
