package org.molr.mole.core.utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Uncheckeds {

    private Uncheckeds() {
        /* Only static methods */
    }

    public static final Runnable unchecked(CheckedThrowingRunnable runnable) {
        return () -> runUnchecked(runnable);
    }

    public static final void runUnchecked(CheckedThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface CheckedThrowingRunnable extends Runnable {
        @Override
        default void run() {
            runUnchecked(() -> checkedThrowingRun());
        }

        void checkedThrowingRun() throws Exception;
    }


    public static final <T> Consumer<T> unchecked(CheckedThrowingConsumer<T> runnable) {
        return (t) -> runUnchecked(() -> runnable.accept(t));
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

}
