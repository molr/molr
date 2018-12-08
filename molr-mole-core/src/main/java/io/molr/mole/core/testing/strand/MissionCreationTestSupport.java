package io.molr.mole.core.testing.strand;

import io.molr.mole.core.runnable.lang.Branch;
import org.slf4j.Logger;

public interface MissionCreationTestSupport {

    default Branch.Task log(String text) {
        return new Branch.Task(text, () -> logger().info("{} executed", text));
    }

    Logger logger();
}
