package io.molr.mole.core.tree;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import io.molr.mole.core.testing.strand.AbstractSingleMissionStrandExecutorTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import static io.molr.commons.domain.RunState.RUNNING;

/**
 * This test makes sure that the {@link ConcurrentStrandExecutor#moveTo(Block)} behaves correctly. To be updated/removed
 * when the moveTo is correctly incorporated into the API
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
        return new RunnableLeafsMissionSupport() {
            {
                sequential("Root", root -> {
                    root.sequential("Sequential", b -> {
                        SEQUENTIAL = latestBlock();

                        log(b, "Sequential A");
                        SEQUENTIAL_LEAF_A = latestBlock();

                        log(b, "Sequential B");
                        SEQUENTIAL_LEAF_B = latestBlock();
                    });

                    root.parallel("Parallel", b -> {
                        PARALLEL = latestBlock();

                        b.run("Parallel A", () -> {
                            unlatch(latchStart);
                            await(latchEnd);
                        });
                        PARALLEL_LEAF_A = latestBlock();

                        log(b, "Parallel B");
                        PARALLEL_LEAF_B = latestBlock();
                    });

                    log(root, "Leaf");
                    LEAF = latestBlock();
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
