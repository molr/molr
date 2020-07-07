package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Out;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.utils.Checkeds;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.molr.mole.core.runnable.lang.BranchMode.PARALLEL;
import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;
import static java.util.Objects.requireNonNull;

public class Branch {

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
        return run(name, (in, out) -> runnable.run());
    }

    public Block run(String name, Checkeds.CheckedThrowingRunnable runnable) {
        return run(name, (in, out) -> runnable.run());
    }

    public Block run(String name, Checkeds.CheckedThrowingConsumer<In> runnable) {
        return run(name, (in, out) -> runnable.accept(in));
    }

    public Block run(String name, Checkeds.CheckedThrowingBiConsumer<In, Out> runnable) {
        return builder.leafChild(parent, name, runnable);
    }

    public OngoingBranch sequential(String name) {
        return new OngoingBranch(name, builder, parent, SEQUENTIAL);
    }

    public OngoingBranch parallel(String name) {
        return new OngoingBranch(name, builder, parent, PARALLEL);
    }

    @Deprecated
    public Block sequential(String name, Consumer<Branch> branchDefiner) {
        return sequential(name).as(branchDefiner);
    }

    @Deprecated
    public Block parallel(String name, Consumer<Branch> branchDefiner) {
        return parallel(name).as(branchDefiner);
    }

    public Block run(Task task) {
        return run(task.name, task.runnable);
    }

    public void println(Object object) {
        run("println(\"" + object + "\");", () -> System.out.println(object));
    }

    public void sleep(long time, TimeUnit unit) {
        run("Sleep " + time + " " + unit, () -> unit.sleep(time));
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
