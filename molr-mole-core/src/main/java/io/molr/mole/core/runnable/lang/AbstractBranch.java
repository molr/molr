package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.utils.Checkeds;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public abstract class AbstractBranch {

    private final RunnableLeafsMission.Builder builder;
    private final Block parent;

    protected AbstractBranch(RunnableLeafsMission.Builder builder, Block parent) {
        this.builder = requireNonNull(builder, "builder must not be null");
        this.parent = requireNonNull(parent, "parent must not be null");
    }

    public abstract GenericOngoingBranch<? extends GenericOngoingBranch<?>> branch(String name, Placeholder<?>... placeholders);

    public abstract GenericOngoingLeaf<? extends GenericOngoingLeaf<?>> leaf(String name, Placeholder<?>... placeholders);

    protected RunnableLeafsMission.Builder builder() {
        return this.builder;
    }

    protected Block parent() {
        return this.parent;
    }

    public void println(Object object) {
        leaf("println(\"" + object + "\");").run(() -> System.out.println(object));
    }

    public void sleep(long time, TimeUnit unit) {
        leaf("Sleep " + time + " " + unit).run(() -> unit.sleep(time));
    }

    /*
    DEPRECATED methods. To be removed asap.
     */

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

}
