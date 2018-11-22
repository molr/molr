package org.molr.mole.core.utils;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Checkeds {

    private Checkeds() {
        /* Only static methods */
    }

    public static final void runUnchecked(CheckedThrowingRunnable runnable) {
        runnable.run();
    }

    @FunctionalInterface
    public interface CheckedThrowingRunnable extends Runnable {
        @Override
        default void run() {
            try {
                checkedThrowingRun();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void checkedThrowingRun() throws Exception;
    }


    public static final <T> T callUnchecked(CheckedThrowingCallable<T> runnable) {
        return runnable.call();
    }

    @FunctionalInterface
    public interface CheckedThrowingCallable<T> extends Callable<T> {
        @Override
        default T call() {
            try {
                return checkedThrowingCall();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        T checkedThrowingCall() throws Exception;
    }

    @FunctionalInterface
    public interface CheckedThrowingConsumer<T> extends Consumer<T> {
        @Override
        default void accept(T t) {
            runUnchecked(() -> checkedThrowingAccept(t));
        }

        void checkedThrowingAccept(T t) throws Exception;
    }

    public static final <T, U> BiConsumer<T, U> unchecked(CheckedThrowingBiConsumer<T, U> runnable) {
        return (t, u) -> runUnchecked(() -> runnable.accept(t, u));
    }

    @FunctionalInterface
    public interface CheckedThrowingBiConsumer<T, U> extends BiConsumer<T, U> {
        @Override
        default void accept(T t, U u) {
            runUnchecked(() -> checkedThrowingAccept(t, u));
        }

        void checkedThrowingAccept(T t, U u) throws Exception;
    }

    @FunctionalInterface
    public interface CheckedThrowingFunction<T, R> extends Function<T, R> {

        @Override
        default R apply(T t) {
            return callUnchecked(() -> checkedThrowingApply(t));
        }

        R checkedThrowingApply(T t) throws Exception;
    }

    public interface CheckedThrowingBiFunction<T, U, R> extends BiFunction<T, U, R> {

        @Override
        default R apply(T t, U u) {
            return callUnchecked(() -> checkedThrowingApply(t, u));
        }

        R checkedThrowingApply(T t, U u) throws Exception;
    }

}
