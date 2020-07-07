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
    public Block run(String name, Runnable runnable) {
        return leaf(name).run(runnable);
    }

    @Deprecated
    public Block run(String name, Checkeds.CheckedThrowingRunnable runnable) {
        return leaf(name).run(runnable);
    }

    @Deprecated
    public Block run(String name, Checkeds.CheckedThrowingConsumer<In> runnable) {
        return leaf(name).run(runnable);
    }

    @Deprecated
    public Block run(String name, Checkeds.CheckedThrowingBiConsumer<In, Out> runnable) {
        return leaf(name).run(runnable);
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

    public Block run(Task task) {
        return leaf(task.name).run(task.runnable);
    }

    public void println(Object object) {
        leaf("println(\"" + object + "\");").run(() -> System.out.println(object));
    }

    public void sleep(long time, TimeUnit unit) {
        leaf("Sleep " + time + " " + unit).run(() -> unit.sleep(time));
    }

    public static class Task {
        private final String name;
        private final Checkeds.CheckedThrowingBiConsumer<In, Out> runnable;

        public Task(String name, Runnable runnable) {
            this(name, (in, out) -> runnable.run());
        }

        public Task(String name, Checkeds.CheckedThrowingBiConsumer<In, Out> runnable) {
            this.name = requireNonNull(name, "name must not be null.");
            this.runnable = requireNonNull(runnable, "runnable must not be null");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Task task = (Task) o;
            return Objects.equals(name, task.name) &&
                    Objects.equals(runnable, task.runnable);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, runnable);
        }

        @Override
        public String toString() {
            return "Task{" +
                    "name='" + name + '\'' +
                    ", runnable=" + runnable +
                    '}';
        }
    }


}
