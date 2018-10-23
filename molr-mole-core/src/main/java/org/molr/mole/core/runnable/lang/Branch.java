package org.molr.mole.core.runnable.lang;

import org.molr.commons.domain.Block;
import org.molr.mole.core.runnable.RunnableLeafsMission;

import java.util.function.Consumer;

public class Branch implements RunnableBranchSupport {

    private final RunnableLeafsMission.Builder builder;
    private final Block parent;

    private Branch(RunnableLeafsMission.Builder builder, Block parent) {
        this.builder = builder;
        this.parent = parent;
    }

    static Branch withParent(RunnableLeafsMission.Builder builder, Block parent) {
        return new Branch(builder, parent);
    }

    public Block run(String name, Runnable runnable) {
        return builder.leafChild(parent, name, runnable);
    }

    public Block sequential(String name, Consumer<Branch> branchDefiner) {
        Block node = builder.sequentialChild(parent, name);
        branchDefiner.accept(Branch.withParent(builder, node));
        return node;
    }

    public Block parallel(String name, Consumer<Branch> branchDefiner) {
        Block node = builder.parallelChild(parent, name);
        branchDefiner.accept(Branch.withParent(builder, node));
        return node;
    }


}
