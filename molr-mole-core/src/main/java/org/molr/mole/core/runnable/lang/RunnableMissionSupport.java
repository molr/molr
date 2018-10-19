package org.molr.mole.core.runnable.lang;

import org.molr.mole.core.runnable.ExecutionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * An abstract class which is intended to describe a tree of runnables, which can be used as simple test case for parallel tree execution.
 */
public abstract class RunnableMissionSupport implements RunnableBranchSupport {

    private final static Logger LOGGER = LoggerFactory.getLogger(RunnableMissionSupport.class);

    private Branch rootBranch = null;
    private String name = null;
    private ExecutionData.Builder builder;

    public void run(String name, Runnable runnable) {
        root().run(name, runnable);
    }

    public void sequential(String name, Consumer<Branch> branchDefiner) {
        root().sequential(name, branchDefiner);
    }

    public void parallel(String name, Consumer<Branch> branchDefiner) {
        root().parallel(name, branchDefiner);
    }

    protected void mission(String newName) {
        this.name = requireNonNull(newName, "name must not be null.");
    }

    private Branch root() {
        if (this.rootBranch == null) {
            this.builder = ExecutionData.builder(missionName());
            this.rootBranch = Branch.withParent(builder, builder.root());
        }
        return this.rootBranch;
    }

    private String missionName() {
        if (this.name == null) {
            LOGGER.warn("Mission name is not set! First DSL call should be to name(String)! Using class name as fallback.");
        }
        return getClass().getSimpleName();
    }

    public ExecutionData build() {
        return builder.build();
    }


}
