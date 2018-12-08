package io.molr.mole.core.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ThreadFactory;

public final class ThreadFactories {

    /**
     * Returns a {@link ThreadFactory} with the specified name format
     */
    public static ThreadFactory namedThreadFactory(String nameFormat) {
        return new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
    }

    private ThreadFactories() {
        throw new UnsupportedOperationException();
    }
}
