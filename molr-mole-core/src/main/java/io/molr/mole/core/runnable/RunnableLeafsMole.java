package io.molr.mole.core.runnable;

import io.molr.commons.domain.*;
import io.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import io.molr.mole.core.tree.*;
import io.molr.mole.core.tree.tracking.TreeTracker;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class RunnableLeafsMole extends AbstractJavaMole {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(RunnableLeafsMole.class);

    private final Map<Mission, RunnableLeafsMission> missions;

    public RunnableLeafsMole(Set<RunnableLeafsMission> missions) {
        super(extractMissions(missions));
        this.missions = createMissionsMap(missions);
    }

    private static Set<Mission> extractMissions(Set<RunnableLeafsMission> missions) {
        requireNonNull(missions, "missions must not be null");
        return missions.stream().map(rlm -> new Mission(rlm.name())).collect(toSet());
    }

    private static Map<Mission, RunnableLeafsMission> createMissionsMap(Set<RunnableLeafsMission> newMissions) {
        return newMissions.stream()
                .collect(toMap(m -> new Mission(m.name()), identity()));
    }

    @Override
    public MissionRepresentation missionRepresentationOf(Mission mission) {
        return getOrThrow(mission).treeStructure().missionRepresentation();
    }

    @Override
    public MissionParameterDescription missionParameterDescriptionOf(Mission mission) {
        return getOrThrow(mission).parameterDescription();
    }


    private RunnableLeafsMission getOrThrow(Mission mission) {
        RunnableLeafsMission runnableMission = missions.get(mission);
        if (runnableMission == null) {
            throw new IllegalArgumentException(mission + " is not a mission of this mole");
        }
        return runnableMission;
    }

    private static void createExpandedMission(RunnableLeafsMission mission, Block block, MissionInput missionInput, String url, ImmutableMissionRepresentation.Builder representationBuilder, Map<Block, MissionInput> scopedInputs, Builder<Block, BiConsumer<In, Out>> updatedRunnablesAfterTraverseBuilder, Set<Block> parallelBlocks) {
    	MissionRepresentation representation = mission.treeStructure().missionRepresentation();
    	Map<Block, ForEachConfiguration<?,?>> forEachConfigs = mission.forEachBlocksConfigurations();


		Block replicatedSubtree=Block.idAndText(url, block.text());
		if(mission.treeStructure().isParallel(block)) {
			parallelBlocks.add(replicatedSubtree);
		}
		
    	if(forEachConfigs.containsKey(block)) {
    		ForEachConfiguration<?, ?> foreachConfig = forEachConfigs.get(block);
    		Collection<?> forEachItems = (Collection<?>)missionInput.get(foreachConfig.collectionPlaceholder());
    		int i=0;
    		for(Object item : forEachItems) {
    			MissionInput scopedInput = missionInput.and(foreachConfig.itemPlaceholder().name(), item);
    			scopedInput = missionInput.and(foreachConfig.transformedItemPlaceholder().name(), foreachConfig.function().apply(scopedInput));    			
    			
    			//mission.contexts
    			if(mission.contexts.containsKey(block)) {
    				ContextConfiguration contextConfig = mission.contexts.get(block);
    				System.out.println("addContext "+block+ contextConfig.contextPlaceholder().name());
    				System.out.println(scopedInput);
    				scopedInput = scopedInput.and(contextConfig.contextPlaceholder().name(), contextConfig.contextFactory().apply(scopedInput));
    			}
    			
    			
    	    	for(Block child : representation.childrenOf(block)) {
    	    		String childUrl = url+"."+i++;
    	    		Block replicatedChild = Block.idAndText(childUrl, child.text());

    	    		representationBuilder.parentToChild(replicatedSubtree, replicatedChild);
    	    		if(representation.isLeaf(child)) {
    	        		scopedInputs.put(replicatedChild, scopedInput);
    	        		updatedRunnablesAfterTraverseBuilder.put(replicatedChild, mission.runnables().get(child));
    	    		}
    	    		else {
        	    		createExpandedMission(mission, child, scopedInput, childUrl, representationBuilder, scopedInputs, updatedRunnablesAfterTraverseBuilder, parallelBlocks);    	    			
    	    		}
    	    	}
    		}
    	}
    	else {
    		
			//mission.contexts
    		MissionInput scopedInput = missionInput;
			if(mission.contexts.containsKey(block)) {
				ContextConfiguration contextConfig = mission.contexts.get(block);
				System.out.println("addContext "+block+ contextConfig.contextPlaceholder().name());
				scopedInput = missionInput.and(contextConfig.contextPlaceholder().name(), contextConfig.contextFactory().apply(missionInput));
				System.out.println(scopedInput);
			}
    		
    		int i=0;
	    	for(Block child : representation.childrenOf(block)) {
	    		String childUrl = url+"."+i++;
	    		Block replicatedChild = Block.idAndText(childUrl, child.text());//replicatedBlock(child, url+subtree.id());
	    		representationBuilder.parentToChild(replicatedSubtree, replicatedChild);
	    		if(representation.isLeaf(child)) {
	        		scopedInputs.put(replicatedChild, scopedInput);	
	        		updatedRunnablesAfterTraverseBuilder.put(replicatedChild, mission.runnables().get(child));
	    		}
	    		else {
		    		createExpandedMission(mission, child, scopedInput, childUrl, representationBuilder, scopedInputs, updatedRunnablesAfterTraverseBuilder, parallelBlocks);	    			
	    		}
	    	}
    	}
    }
    
    @Override
    protected MissionExecutor executorFor(Mission mission, Map<String, Object> params) {
        RunnableLeafsMission runnableLeafMission = missions.get(mission);
        MissionInput input = missionInput(runnableLeafMission, params);
        
        /*
         * create structure with expanded foreach blocks
         */     
        ImmutableMissionRepresentation.Builder newRepresentationBuilder = ImmutableMissionRepresentation.builder(runnableLeafMission.treeStructure().rootBlock());
        Map<Block, MissionInput> scopedInputs = new HashMap<>();
        final ImmutableMap.Builder<Block, BiConsumer<In, Out>> updatedRunnablesAfterTraverseBuilder = ImmutableMap.builder();
        Set<Block> newParallelBlocks = new HashSet<>();
        Block rootBlock = runnableLeafMission.treeStructure().missionRepresentation().rootBlock();
        createExpandedMission(runnableLeafMission, rootBlock, input, rootBlock.id(), newRepresentationBuilder, scopedInputs, updatedRunnablesAfterTraverseBuilder, newParallelBlocks);

        TreeStructure updatedTreeStructure = new TreeStructure(newRepresentationBuilder.build(), ImmutableSet.copyOf(newParallelBlocks));
        TreeTracker<Result> resultTracker = TreeTracker.create(updatedTreeStructure.missionRepresentation(), Result.UNDEFINED, Result::summaryOf);
        TreeTracker<RunState> runStateTracker = TreeTracker.create(updatedTreeStructure.missionRepresentation(), RunState.UNDEFINED, RunState::summaryOf);

        MissionOutputCollector outputCollector = new ConcurrentMissionOutputCollector();

        ExecutionStrategy executionStrategy = inferExecutionStrategyFromParameters(missionParameterDescriptionOf(mission), input);
        LOGGER.info("ExecutionStrategy: "+executionStrategy);
        //LeafExecutor leafExecutor = new RunnableBlockExecutor(resultTracker, updatedRunnables, input, blockInputs, outputCollector, runStateTracker);
        LeafExecutor leafExecutor = new RunnableBlockExecutor(resultTracker, updatedRunnablesAfterTraverseBuilder.build(), input, scopedInputs, outputCollector, runStateTracker);
        return new TreeMissionExecutor(updatedTreeStructure, leafExecutor, resultTracker, outputCollector, runStateTracker, executionStrategy);
    }
    
    private static MissionInput missionInput(RunnableLeafsMission mission, Map<String, Object> params) {
        MissionInput in = MissionInput.from(params);

//        Function<In, ?> contextFactory = mission.contextFactory();
//        if (contextFactory == null) {
//            return in;
//        }
        System.out.println("ctc");
        System.out.println(mission.contexts);
       for(ContextConfiguration config: mission.contexts.values()) {
    	   System.out.println(in);
    	   //in = in.and(placeholder.name(), mission.contexts.get(placeholder).apply(in));
       }
       return in;//.and(Placeholders.context().name(), contextFactory.apply(in));
    }
    
    private static ExecutionStrategy inferExecutionStrategyFromParameters(MissionParameterDescription parameterDescription, MissionInput input) {
        ExecutionStrategy executionStrategy = ExecutionStrategy.PAUSE_ON_ERROR;
        if(parameterDescription.hasParameterForPlaceholder(Placeholders.EXECUTION_STRATEGY)) {
            if(input.get(Placeholders.EXECUTION_STRATEGY) != null) {
                String executionStrategyString = input.get(Placeholders.EXECUTION_STRATEGY);
                executionStrategy = ExecutionStrategy.forName(executionStrategyString);
            }
        }
        else {
            LOGGER.warn("Selected ExecutionStrategy has been ignored since corresponding parameter is not specified in parameter description");
        }

        /*
         * Exception and/or log entry if lenient mode parameter is provided by params but not defined in mission definition
         */
        return executionStrategy;
    }

}
