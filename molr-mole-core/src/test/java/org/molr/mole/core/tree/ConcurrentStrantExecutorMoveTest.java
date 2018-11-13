package org.molr.mole.core.tree;

import org.junit.Before;
import org.junit.Test;
import org.molr.commons.domain.Block;
import org.molr.commons.domain.StrandCommand;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.molr.mole.core.tree.support.AbstractSingleMissionStrandExecutorTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import static org.molr.commons.domain.RunState.RUNNING;

/**
 * This test makes sure that the {@link ConcurrentStrandExecutor#moveTo(Block)} behaves correctly.
 * To be updated/removed when the moveTo is correctly incorporated into the API
 */
@Deprecated
public class ConcurrentStrantExecutorMoveTest extends AbstractSingleMissionStrandExecutorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentStrandExecutorTest.class);

    private Block SEQUENTIAL;
    private Block SEQUENTIAL_LEAF_A;
    private Block SEQUENTIAL_LEAF_B;
    private Block PARALLEL;
    private Block PARALLEL_LEAF_A;
    private Block PARALLEL_LEAF_B;
    private Block LEAF;

    private CountDownLatch latchStart;
    private CountDownLatch latchEnd;

    @Override
    protected RunnableLeafsMission mission() {
        return new RunnableMissionSupport() {
            {
                mission("Root", root -> {
                    SEQUENTIAL = root.sequential("Sequential", b -> {
                        SEQUENTIAL_LEAF_A = b.run(log("Sequential A"));
                        SEQUENTIAL_LEAF_B = b.run(log("Sequential B"));
                    });

                    PARALLEL = root.parallel("Parallel", b -> {
                        PARALLEL_LEAF_A = b.run("Parallel A", () -> {
                            unlatch(latchStart);
                            await(latchEnd);
                        });
                        PARALLEL_LEAF_B = b.run(log("Parallel B"));
                    });

                    LEAF = root.run(log("Leaf"));
                });
            }
        }.build();
    }

    @Before
    public void setUp() {
        latchStart = new CountDownLatch(1);
        latchEnd = new CountDownLatch(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveIntoAnUnknownBlockThrows() {
        moveRootStrandTo(Block.idAndText("unknown", "unknown"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveIntoParallelANodeIsNotSupported() {
        moveRootStrandTo(PARALLEL_LEAF_A);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveIntoParallelBNodeIsNotSupported() {
        moveRootStrandTo(PARALLEL_LEAF_B);
    }

    @Test(expected = IllegalStateException.class)
    public void testMoveToWhileRunningFails() {
        instructRootStrandAsync(StrandCommand.RESUME);

        await(latchStart);
        waitUntilRootStrandStateIs(RUNNING);

        moveRootStrandTo(SEQUENTIAL);
    }

    @Test
    public void testMoveIntoSequentialAndLeafNodes() {
        Stream.of(SEQUENTIAL, SEQUENTIAL_LEAF_A, SEQUENTIAL_LEAF_B, PARALLEL, LEAF).forEach(this::moveRootStrandTo);
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }
}
