package io.molr.mole.core.tree;

import static io.molr.commons.domain.RunState.FINISHED;
import static io.molr.commons.domain.RunState.RUNNING;

import java.util.Map;
import java.util.function.BiConsumer;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.MissionInput;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import io.molr.mole.core.tree.tracking.Bucket;
import io.molr.mole.core.tree.tracking.TreeTracker;

public class StateTrackingBlockExecutor extends RunnableBlockExecutor{

	private final Bucket<Result> resultTracker;
	private final TreeTracker<RunState> runStateTracker;
	
	public StateTrackingBlockExecutor(Bucket<Result> resultTracker, Map<Block, BiConsumer<In, Out>> runnables,
			MissionInput input, Map<Block, MissionInput> scopedInputs, MissionOutputCollector outputCollector,
			TreeTracker<RunState> runStateTracker) {
		super(runnables, input, scopedInputs, outputCollector);
		this.resultTracker = resultTracker;
		this.runStateTracker = runStateTracker;
	}
	
	@Override
	protected void doBeforeExecute(Block block) {
        runStateTracker.push(block, RUNNING);
	}

	@Override
	protected void doAfterExecute(Block block, Result result) {
        resultTracker.push(block, result);
        runStateTracker.push(block, FINISHED);		
	}


}
