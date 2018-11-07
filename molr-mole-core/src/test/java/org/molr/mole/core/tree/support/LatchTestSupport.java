package org.molr.mole.core.tree.support;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Support interface that provides default methods for handling {@link CountDownLatch}es.
 */
public interface LatchTestSupport {

    default void unlatch(CountDownLatch... latches) {
        for (CountDownLatch latch : latches) {
            unlatch(latch);
        }
    }

    default void unlatch(CountDownLatch latch) {
        latch.countDown();
    }

    default void await(CountDownLatch... latches) {
        for (CountDownLatch latch : latches) {
            await(latch);
        }
    }

    default void await(CountDownLatch latch) {
        try {
            if (!latch.await(1, TimeUnit.MINUTES)) {
                throw new RuntimeException("Latch timed out");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
