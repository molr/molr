package org.molr.mole.core.tree.support;

import java.util.concurrent.CountDownLatch;

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
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
