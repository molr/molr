package io.molr.mole.core.utils;

import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public final class ThreadFactories {

    /**
     * Returns a {@link ThreadFactory} with the specified name format
     */
    public static ThreadFactory namedDaemonThreadFactory(String nameFormat) {
        return new ThreadFactoryBuilder().setNameFormat(nameFormat).setDaemon(true).build();
    }

    private ThreadFactories() {
        throw new UnsupportedOperationException();
    }
}
