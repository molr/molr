package io.molr.mole.core.runnable.lang.ctx;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.Branch;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.OngoingLeaf;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;
import static java.util.Objects.requireNonNull;

public class ContextualBranch<C> {

    private final RunnableLeafsMission.Builder builder;
    private final Block parent;
    private final Function<In, C> contextFactory;

    protected ContextualBranch(RunnableLeafsMission.Builder builder, Block parent, Function<In, C> contextFactory) {
        this.builder = requireNonNull(builder, "builder must not be null");
        this.parent = requireNonNull(parent, "parent must not be null");
        this.contextFactory = requireNonNull(contextFactory, "contextFactory must not be null.");
    }

    public OngoingContextualBranch<C> branch(String name) {
        return new OngoingContextualBranch<>(name, builder, parent, SEQUENTIAL, contextFactory);
    }


    public void println(Object object) {
        leaf("println(\"" + object + "\");").run(() -> System.out.println(object));
    }

    public void sleep(long time, TimeUnit unit) {
        leaf("Sleep " + time + " " + unit).run(() -> unit.sleep(time));
    }


    public OngoingContextualLeaf leaf(String name) {
        return new OngoingContextualLeaf(name, builder, parent, contextFactory);
    }


}
