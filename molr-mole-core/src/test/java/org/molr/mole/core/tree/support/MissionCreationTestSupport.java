package org.molr.mole.core.tree.support;

import org.molr.mole.core.runnable.lang.RunnableBranchSupport;
import org.slf4j.Logger;

public interface MissionCreationTestSupport {

    default RunnableBranchSupport.Task log(String text) {
        return new RunnableBranchSupport.Task(text, () -> logger().info("{} executed", text));
    }

    Logger logger();
}
