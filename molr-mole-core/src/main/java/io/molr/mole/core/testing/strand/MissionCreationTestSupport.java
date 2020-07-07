package io.molr.mole.core.testing.strand;

import io.molr.commons.domain.Block;
import io.molr.mole.core.runnable.lang.Branch;
import org.slf4j.Logger;

public interface MissionCreationTestSupport {

    default Block log(Branch b, String text) {
        return b.leaf(text).run(() -> logger().info("{} executed", text));
    }

    Logger logger();
}
