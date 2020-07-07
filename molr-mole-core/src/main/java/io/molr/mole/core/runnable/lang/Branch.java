package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Out;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.utils.Checkeds;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class Branch {

    private final RunnableLeafsMission.Builder builder;
    private final Block parent;

    private Branch(RunnableLeafsMission.Builder builder, Block parent) {
        this.builder = requireNonNull(builder, "builder must not be null");
        this.parent = requireNonNull(parent, "parent must not be null");
    }

    static Branch withParent(RunnableLeafsMission.Builder builder, Block parent) {
        return new Branch(builder, parent);
    }

    @Deprecated
    public void run(String name, Runnable runnable) {
        leaf(name).run(runnable);
    }

    @Deprecated
    public void run(String name, Checkeds.CheckedThrowingRunnable runnable) {
        leaf(name).run(runnable);
    }

    @Deprecated
    public void run(String name, Checkeds.CheckedThrowingConsumer<In> runnable) {
        leaf(name).run(runnable);
    }

    @Deprecated
    public void run(String name, Checkeds.CheckedThrowingBiConsumer<In, Out> runnable) {
        leaf(name).run(runnable);
    }

    public OngoingLeaf leaf(String name) {
        return new OngoingLeaf(name, builder, parent);
    }

    @Deprecated
    public void sequential(String name, Consumer<Branch> branchDefiner) {
        this.branch(name).sequential().as(branchDefiner);
    }

    @Deprecated
    public void parallel(String name, Consumer<Branch> branchDefiner) {
        branch(name).parallel().as(branchDefiner);
    }

    public OngoingBranch branch(String name) {
        return new OngoingBranch(name, builder, parent);
    }


    public void println(Object object) {
        leaf("println(\"" + object + "\");").run(() -> System.out.println(object));
    }

    public void sleep(long time, TimeUnit unit) {
        leaf("Sleep " + time + " " + unit).run(() -> unit.sleep(time));
    }

}
