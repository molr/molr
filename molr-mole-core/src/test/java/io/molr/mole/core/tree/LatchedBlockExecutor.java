package io.molr.mole.core.tree;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.MissionInput;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Result;
import io.molr.mole.core.runnable.exec.RunnableBlockExecutor;

public class LatchedBlockExecutor extends RunnableBlockExecutor{

	Map<Block, CountDownLatch> latches = new ConcurrentHashMap<>();
	Map<Block, CountDownLatch> exitLatches = new ConcurrentHashMap<>();
	
	public LatchedBlockExecutor(Map<Block, BiConsumer<In, Out>> runnables, MissionInput input,
			Map<Block, MissionInput> scopedInputs, MissionOutputCollector outputCollector) {
		super(runnables, input, scopedInputs, outputCollector);
		runnables.keySet().forEach(block -> {
			latches.put(block, new CountDownLatch(1));
			exitLatches.put(block, new CountDownLatch(1));
		});
	}
	
	@Override
	protected void doBeforeExecute(Block block) {
		super.doBeforeExecute(block);
	}
	
	@Override
	protected void doAfterExecute(Block block, Result result) {
		try {
			latches.get(block).countDown();
			exitLatches.get(block).await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void unlatchAll(){
		latches.forEach((block, latch) ->{
			latch.countDown();
			exitLatches.get(block).countDown();
		});
	}
	
	public void unlatch(Block block) {
		exitLatches.get(block).countDown();
	}
	
	public void awaitEntry(Block block) {
		latches.get(block).countDown();
	}

}
