package io.molr.mole.core.testing.strand;

import io.molr.mole.core.runnable.lang.SimpleBranch;
import org.slf4j.Logger;

public interface MissionCreationTestSupport {

    default void log(SimpleBranch b, String text) {
        b.leaf(text).run(() -> logger().info("{} executed", text));
    }

    Logger logger();
}
