package org.molr.mole.core.runnable.lang;

import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * An abstract class which is intended to describe a tree of runnables, which can be used as simple test case for parallel tree execution.
 */
public abstract class RunnableMissionSupport {

    private final static Logger LOGGER = LoggerFactory.getLogger(RunnableMissionSupport.class);

    private RunnableLeafsMission.Builder builder;

    protected void mission(String newName, Consumer<Branch> branchConsumer) {
        if (this.builder != null) {
            throw new IllegalStateException("Root can only be defined once!");
        }

        requireNonNull(newName, "name must not be null.");
        requireNonNull(branchConsumer, "branchConsumer must not be null.");

        Branch rootBranch = root(newName);
        branchConsumer.accept(rootBranch);
    }

    private Branch root(String name) {
        this.builder = RunnableLeafsMission.builder(name);
        return Branch.withParent(builder, builder.root());
    }

    public RunnableLeafsMission build() {
        return builder.build();
    }


}
