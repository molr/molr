package io.molr.mole.core.tree;

import static io.molr.commons.domain.Result.FAILED;
import static io.molr.commons.domain.Result.SUCCESS;
import static io.molr.commons.util.Exceptions.stackTraceFrom;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MissionInput;
import io.molr.commons.domain.Placeholders;
import io.molr.commons.domain.Result;

public abstract class LeafExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeafExecutor.class);

    private final MissionInput input;
    private final Map<Block, MissionInput> blockInputs;
    private final MissionOutputCollector output;

    protected LeafExecutor(MissionInput input, Map<Block, MissionInput> scopedInputs, MissionOutputCollector output) {
        this.input = input;
        this.blockInputs = scopedInputs;
        this.output = output;
    }

    protected MissionInput input() {
        return this.input;
    }
    
    protected MissionInput combinedMissionInput(Block block) {
    	if(blockInputs.containsKey(block)) {
        	return blockInputs.get(block);    		
    	}
    	return input();

    }

    protected BlockOutputCollector outputFor(Block block) {
        return new BlockOutputCollector(output, block);
    }

    
    public final Result execute(Block block) {
    	doBeforeExecute(block);
        Result result = tryCatchExecute(block);
        doAfterExecute(block, result);
        return result;
    }

    protected abstract void doBeforeExecute(Block block);
    
    protected abstract void doAfterExecute(Block block, Result result);
    
    private final Result tryCatchExecute(Block block) {
        try {
            doExecute(block);
            return SUCCESS;
        } catch (Exception e) {
            LOGGER.warn("Execution of {} threw an exception: {}", block, e.getMessage(), e);
            outputFor(block).emit(Placeholders.THROWN, stackTraceFrom(e));
            outputFor(block).emit(Placeholders.THROWN.name()+".message", e.getMessage());
            return FAILED;
        }
    }


    protected abstract void doExecute(Block block);

}
