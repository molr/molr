package io.molr.mole.core.runnable.exec;

import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.MissionInput;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Result;
import io.molr.mole.core.tree.LeafExecutor;
import io.molr.mole.core.tree.MissionOutputCollector;

/**
 * Leaf executor that relates {@link Runnable} with {@link Block} for the leaf execution logic.
 * <p>
 * The result of a leaf is considered {@link Result#SUCCESS} if the execution does not throw any exception.
 */
public class RunnableBlockExecutor extends LeafExecutor {

    private final Map<Block, BiConsumer<In, Out>> runnables;

    public RunnableBlockExecutor(Map<Block, BiConsumer<In, Out>> runnables, MissionInput input,
            Map<Block, MissionInput> scopedInputs, MissionOutputCollector outputCollector) {
        super(input, scopedInputs, outputCollector);
        this.runnables = ImmutableMap.copyOf(runnables);
    }

    @Override
    protected void doExecute(Block block) {
        MissionInput input = combinedMissionInput(block);
        runnables.get(block).accept(input, outputFor(block));
    }

    @Override
    protected void doBeforeExecute(Block block) {
        /* empty per default. Can be overridden */
    }

    @Override
    protected void doAfterExecute(Block block, Result result) {
        /* empty per default. Can be overridden */
    }

}
