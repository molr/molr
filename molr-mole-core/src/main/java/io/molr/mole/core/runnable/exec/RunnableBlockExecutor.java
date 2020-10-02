package io.molr.mole.core.runnable.exec;

import com.google.common.collect.ImmutableMap;
import io.molr.commons.domain.*;
import io.molr.mole.core.tree.LeafExecutor;
import io.molr.mole.core.tree.MissionOutputCollector;
import io.molr.mole.core.tree.tracking.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Leaf executor that relates {@link Runnable} with {@link Block} for the leaf execution logic.
 * <p>
 * The result of a leaf is considered {@link Result#SUCCESS} if the execution does not throw any exception.
 */
public class RunnableBlockExecutor extends LeafExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnableBlockExecutor.class);

    private final Map<Block, BiConsumer<In, Out>> runnables;

    public RunnableBlockExecutor(Bucket<Result> resultTracker, Map<Block, BiConsumer<In, Out>> runnables, MissionInput input, Map<Block, MissionInput> scopedInputs, MissionOutputCollector outputCollector, Bucket<RunState> runStateBucket) {
        super(resultTracker, runStateBucket, input, scopedInputs, outputCollector);
        this.runnables = ImmutableMap.copyOf(runnables);
    }

    @Override
    protected void doExecute(Block block) {
    	MissionInput input = combinedMissionInput(block);
        runnables.get(block).accept(input, outputFor(block));
    }

}
