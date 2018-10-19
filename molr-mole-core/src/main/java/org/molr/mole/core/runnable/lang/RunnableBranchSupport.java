package org.molr.mole.core.runnable.lang;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public interface RunnableBranchSupport {

    void run(String name, Runnable runnable);

    void sequential(String name, Consumer<Branch> branchDefiner);

    void parallel(String name, Consumer<Branch> branchDefiner);

    default void println(Object object) {
        run("println(\"" + object + "\");", () -> System.out.println(object));
    }

    default void sleep(long time, TimeUnit unit) {
        run("Sleep " + time + " " + unit, () -> {
            try {
                unit.sleep(time);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
