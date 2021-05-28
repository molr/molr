package io.molr.mole.core.runnable;

import io.molr.commons.domain.*;
import io.molr.mole.core.tree.*;
import io.molr.mole.core.tree.tracking.TreeTracker;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;


import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;

import java.util.List;

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
    
    private static void replicateAndExpandMissionTree(RunnableLeafsMission mission, Block subTree, Block replicatedSubtree, int level, MissionInput missionInput, MissionInput scopedInput, IntantiatedMissionTree.Builder builder) {
    	if(mission.blockScopes.containsKey(subTree)) {
    		System.out.println("found scope update\n\n");
    		builder.addBlockInput(replicatedSubtree, scopedInput);
    		Map<Placeholder<?>, Placeholder<?>> updates = mission.blockScopes.get(subTree);
    		//MissionInput updatedScope = scopedInput;
    		for(Placeholder<?> p : updates.keySet()) {
    			System.out.println(scopedInput);
    			scopedInput = scopedInput.and(updates.get(p).name(), scopedInput.get(updates.get(p)));
    		}
    		
    	}
    	System.out.println(mission.blockScopes);
    	if(mission.treeStructure().missionRepresentation().blockAttributes().containsKey(subTree)) {
        	builder.addBlockAttributes(replicatedSubtree, mission.treeStructure().missionRepresentation().blockAttributes().get(subTree));
    	}
    	
    	if(mission.treeStructure().childrenOf(subTree).isEmpty()) {
    		builder.addBlockInput(replicatedSubtree, scopedInput);
    		builder.addRunnable(replicatedSubtree, mission.runnables().get(subTree));
    	}
    	else {
    		if(mission.treeStructure().isParallel(subTree)) {
    			int maxConcurrency = mission.maxConcurrency().get(subTree);
    			builder.addToParallelBlocks(replicatedSubtree, maxConcurrency);
    		}
    		
    		final MissionInput extendedByContext;
    		if(mission.contexts().containsKey(subTree)) {
				ContextConfiguration contextConfig = mission.contexts().get(subTree);
				extendedByContext = scopedInput.and(contextConfig.contextPlaceholder().name(), contextConfig.contextFactory().apply(scopedInput));
			}
    		else {
    			extendedByContext = scopedInput;
    		}
    		
    		if(mission.forEachBlocksConfigurations().containsKey(subTree)) {
    			ForEachConfiguration<?, ?> foreachConfig = mission.forEachBlocksConfigurations().get(subTree);
    			Collection<?> forEachItems = (Collection<?>)scopedInput.get(foreachConfig.collectionPlaceholder());
    			AtomicInteger childIndex = new AtomicInteger(0);
    			forEachItems.forEach(item->{
    				
        			MissionInput newScopedInput = extendedByContext.and(foreachConfig.itemPlaceholder().name(), item);
        			if(foreachConfig.itemPlaceholder()!=foreachConfig.transformedItemPlaceholder()) {
        				newScopedInput = newScopedInput.and(foreachConfig.transformedItemPlaceholder().name(), foreachConfig.function().apply(newScopedInput));
        			}
        			
        			if(mission.treeStructure().childrenOf(subTree).size()!=1) {
        				throw new IllegalStateException("foreach blocks must have exactly one child.");
        			}
    				Block child = mission.treeStructure().childrenOf(subTree).get(0);
    				addChildToReplicatedTreeAndTraverse(mission, child, replicatedSubtree, missionInput, newScopedInput, childIndex.getAndIncrement(), builder, level);
    			});
    		}
    		else {
    			AtomicInteger childIndex=new AtomicInteger();
        		mission.treeStructure().childrenOf(subTree).forEach(child->{
        			addChildToReplicatedTreeAndTraverse(mission, child, replicatedSubtree, missionInput, extendedByContext, childIndex.getAndIncrement(), builder, level);
        		});
    		}
    	}
    }

    private static void addChildToReplicatedTreeAndTraverse(RunnableLeafsMission mission, Block child, Block replicatedSubtree, MissionInput missionInput, MissionInput newScopedInput, int index, IntantiatedMissionTree.Builder builder, int level) {
		String childText = child.text();
		if(mission.blockNameFormatterArgs(child)!=null) {
			Object[] args = new Object[mission.blockNameFormatterArgs(child).size()];
			List<Placeholder<?>> placeholders = mission.blockNameFormatterArgs(child);
			for (int i = 0; i < args.length; i++) {
				args[i]=newScopedInput.get(placeholders.get(i));
			}
			childText = MessageFormatter.arrayFormat(child.text(), args).getMessage();
		}

		Block replicatedChild = Block.idAndText(replicatedSubtree.id()+"."+index, childText);
		builder.addChild(replicatedSubtree, replicatedChild);
		replicateAndExpandMissionTree(mission, child, replicatedChild, level+1, missionInput, newScopedInput, builder);
    }
   
    @Override
    protected MissionExecutor executorFor(Mission mission, Map<String, Object> params) {
        RunnableLeafsMission runnableLeafMission = missions.get(mission);
        MissionInput input = missionInput(runnableLeafMission, params);
        
        /*
         * create structure with expanded foreach blocks
         */     
        Block rootBlock = runnableLeafMission.treeStructure().missionRepresentation().rootBlock();
        IntantiatedMissionTree.Builder instantiatedBuilder = new IntantiatedMissionTree.Builder();
        replicateAndExpandMissionTree(runnableLeafMission, rootBlock, rootBlock, 0, input, input, instantiatedBuilder);
        IntantiatedMissionTree instantiatedTree = instantiatedBuilder.build();

        TreeStructure updatedTreeStructure = instantiatedTree.getUpdatedTreeStructure();
        LOGGER.info("Instantiated mission tree:\n"+TreeStructure.print(updatedTreeStructure));
        TreeTracker<Result> resultTracker = TreeTracker.create(updatedTreeStructure.missionRepresentation(), Result.UNDEFINED, Result::summaryOf);
        TreeTracker<RunState> runStateTracker = TreeTracker.create(updatedTreeStructure.missionRepresentation(), RunState.NOT_STARTED, RunState::summaryOf);

        MissionOutputCollector outputCollector = new ConcurrentMissionOutputCollector();

        ExecutionStrategy executionStrategy = inferExecutionStrategyFromParameters(missionParameterDescriptionOf(mission), input);
        LOGGER.info("ExecutionStrategy: "+executionStrategy);
        /*
         * TODO remove tracker
         */
        LeafExecutor leafExecutor = new StateTrackingBlockExecutor(resultTracker, instantiatedTree.getRunnables(), input, instantiatedTree.getBlockInputs(), outputCollector, runStateTracker);
        return new TreeMissionExecutor(updatedTreeStructure, leafExecutor, resultTracker, outputCollector, runStateTracker, executionStrategy);
    }
    
    private static MissionInput missionInput(RunnableLeafsMission mission, Map<String, Object> params) {
    	MissionInput in = MissionInput.from(params);
    	return in;
    }
    
    private static ExecutionStrategy inferExecutionStrategyFromParameters(MissionParameterDescription parameterDescription, MissionInput input) {
        ExecutionStrategy executionStrategy = ExecutionStrategy.PAUSE_ON_ERROR;
        if(parameterDescription.hasParameterForPlaceholder(Placeholders.EXECUTION_STRATEGY)) {
            if(input.get(Placeholders.EXECUTION_STRATEGY) != null) {
                String executionStrategyString = input.get(Placeholders.EXECUTION_STRATEGY);
                executionStrategy = ExecutionStrategy.forName(executionStrategyString);
            }
            else {
            	@SuppressWarnings("unchecked")
				MissionParameter<String> executionStrategyParam = (MissionParameter<String>) parameterDescription.parameters().stream().filter(parameter -> {
            		return parameter.placeholder().equals(Placeholders.EXECUTION_STRATEGY);}).findFirst().get();
            	if(executionStrategyParam.defaultValue()!=null) {
            		executionStrategy = ExecutionStrategy.forName(executionStrategyParam.defaultValue());
            	}
            		
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
