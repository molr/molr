package io.molr.mole.core.testing.strand;

import org.slf4j.Logger;

import io.molr.mole.core.runnable.lang.SimpleBranch;

public interface MissionCreationTestSupport {

    default void log(SimpleBranch b, String text) {
        b.leaf(text).run(() -> logger().info("{} executed", text));
    }

    Logger logger();
}
