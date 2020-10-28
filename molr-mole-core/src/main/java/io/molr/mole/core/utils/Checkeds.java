package io.molr.mole.core.utils;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import io.molr.mole.core.utils.function.Consumer3;
import io.molr.mole.core.utils.function.Consumer4;
import io.molr.mole.core.utils.function.Consumer5;

public final class Checkeds {

    private Checkeds() {
        /* Only static methods */
    }

    public static void runUnchecked(CheckedThrowingRunnable runnable) {
        runnable.run();
    }

    @FunctionalInterface
    public interface CheckedThrowingRunnable extends Runnable {
        @Override
        default void run() {
            try {
                checkedThrowingRun();
            } catch (Exception | AssertionError e) {
                throw runtimeException(e);
            }
        }

        void checkedThrowingRun() throws Exception;
    }


    public static <T> T callUnchecked(CheckedThrowingCallable<T> runnable) {
        return runnable.call();
    }

    @FunctionalInterface
    public interface CheckedThrowingCallable<T> extends Callable<T> {
        @Override
        default T call() {
            try {
                return checkedThrowingCall();
            } catch (Exception | AssertionError e) {
                throw runtimeException(e);
            }
        }

        T checkedThrowingCall() throws Exception;
    }

    private static RuntimeException runtimeException(Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException(e);
    }

    @FunctionalInterface
    public interface CheckedThrowingConsumer<T> extends Consumer<T> {
        @Override
        default void accept(T t) {
            runUnchecked(() -> checkedThrowingAccept(t));
        }

        void checkedThrowingAccept(T t) throws Exception;
    }

    public static <T, U> BiConsumer<T, U> unchecked(CheckedThrowingBiConsumer<T, U> runnable) {
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
    public interface CheckedThrowingConsumer3<P1, P2, P3> extends Consumer3<P1, P2, P3> {
        @Override
        default void accept(P1 p1, P2 p2, P3 p3) {
            runUnchecked(() -> checkedThrowingAccept(p1, p2, p3));
        }

        void checkedThrowingAccept(P1 p1, P2 p2, P3 p3) throws Exception;
    }

    @FunctionalInterface
    public interface CheckedThrowingConsumer4<P1, P2, P3, P4> extends Consumer4<P1, P2, P3, P4> {
        @Override
        default void accept(P1 p1, P2 p2, P3 p3, P4 p4) {
            runUnchecked(() -> checkedThrowingAccept(p1, p2, p3, p4));
        }

        void checkedThrowingAccept(P1 p1, P2 p2, P3 p3, P4 p4) throws Exception;
    }

    @FunctionalInterface
    public interface CheckedThrowingConsumer5<P1, P2, P3, P4, P5> extends Consumer5<P1, P2, P3, P4, P5> {
        @Override
        default void accept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5) {
            runUnchecked(() -> checkedThrowingAccept(p1, p2, p3, p4, p5));
        }

        void checkedThrowingAccept(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5) throws Exception;
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
