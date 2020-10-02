package io.molr.mole.core.runnable;

import io.molr.commons.domain.*;
import io.molr.mole.core.runnable.exec.RunnableBlockExecutor;
import io.molr.mole.core.tree.*;
import io.molr.mole.core.tree.tracking.TreeTracker;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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

    private static Block replicatedBlock(Block block, String prefix) {
    	return Block.idAndText(prefix+block.id(), block.text());
    }

    private static void traverse(RunnableLeafsMission mission, Block parent, MissionInput missionInput, String url, ImmutableMissionRepresentation.Builder representationBuilder, Map<Block, MissionInput> scopedInputs, Builder<Block, BiConsumer<In, Out>> updatedRunnablesAfterTraverseBuilder) {
    	MissionRepresentation representation = mission.treeStructure().missionRepresentation();
    	Map<Block, ForEachConfiguration<?,?>> forEachCOnfigs = mission.getForEachBlocksConfigurations();
    	System.out.println("goDown: "+parent+" "+url);
    	//ifForEach add item to block input
    	if(representation.isLeaf(parent)) {

    	}
    	if(forEachCOnfigs.containsKey(parent)) {
    		ForEachConfiguration<?, ?> foreachConfig = forEachCOnfigs.get(parent);
    		System.out.println("forEach");
    		Collection<?> forEachItems = (Collection<?>)missionInput.get(foreachConfig.collectionPlaceholder());
    		for(Object item : forEachItems) {
    			MissionInput scopedInput = missionInput.and(foreachConfig.itemPlaceholder().name(), item);
    	    	for(Block child : representation.childrenOf(parent)) {
    	    		Block replicatedParent=replicatedBlock(parent, url);
    	    		Block replicatedChild = replicatedBlock(child, url+parent.id()+item);
    	    		representationBuilder.parentToChild(replicatedParent, replicatedChild);
    	    		if(representation.isLeaf(child)) {
    	        		scopedInputs.put(replicatedChild, scopedInput);
    	        		updatedRunnablesAfterTraverseBuilder.put(replicatedChild, mission.runnables().get(child));
    	        		System.out.println("leaf "+parent + missionInput);
    	    		}
    	    		else {
        	    		traverse(mission, child, scopedInput, url+parent.id()+item, representationBuilder, scopedInputs, updatedRunnablesAfterTraverseBuilder);    	    			
    	    		}
    	    	}
    		}
    		System.out.println(forEachItems);
    	}
    	else {
	    	for(Block child : representation.childrenOf(parent)) {
	    		Block replicatedParent=replicatedBlock(parent, url);
	    		Block replicatedChild = replicatedBlock(child, url+parent.id());
	    		representationBuilder.parentToChild(replicatedParent, replicatedChild);
	    		if(representation.isLeaf(child)) {
	        		scopedInputs.put(replicatedChild, missionInput);
	        		System.out.println("Runnables "+mission.runnables());
	        		updatedRunnablesAfterTraverseBuilder.put(replicatedChild, mission.runnables().get(child));
	        		System.out.println("leaf "+parent + missionInput);
	    		}
	    		else {
		    		traverse(mission, child, missionInput, url+parent.id(), representationBuilder, scopedInputs, updatedRunnablesAfterTraverseBuilder);	    			
	    		}
	    	}
    	}

    	System.out.println("goUp"+parent);
    }
    
    @Override
    protected MissionExecutor executorFor(Mission mission, Map<String, Object> params) {
        RunnableLeafsMission runnableLeafMission = missions.get(mission);
        MissionInput input = missionInput(runnableLeafMission, params);

        System.out.println(runnableLeafMission.runnables());
        System.out.println(runnableLeafMission.treeStructure().missionRepresentation().parentsToChildren());
        
        
        /*
         * structural updates for for each loops
         */
        ImmutableMissionRepresentation.Builder updatedRepresentationBuilder = ImmutableMissionRepresentation.builder(runnableLeafMission.treeStructure().missionRepresentation());
        final ImmutableMap.Builder<Block, BiConsumer<In, Out>> updatedRunnablesBuilder = ImmutableMap.builder();
        updatedRunnablesBuilder.putAll(runnableLeafMission.runnables());

        /*
         * building of block id should be refactored 
         */
        long maxId = Long.MIN_VALUE;
        for(Block block : runnableLeafMission.treeStructure().allBlocks()){
            long blockId = Long.valueOf(block.id());
            maxId = Long.max(maxId, blockId);
        }
        AtomicLong blockIdTracker = new AtomicLong(maxId+1);//IDs are originally generated by RunnableLeafsMission.Builder
        AtomicInteger forEachCounter = new AtomicInteger(0);
        if (!runnableLeafMission.forEachConfigurations().isEmpty()) {
            runnableLeafMission.forEachConfigurations().forEach((block, configuration) -> {


                Collection<?> devices = (Collection<?>) input.get(configuration.collectionPlaceholder());
                devices.forEach(devcie -> {
                    long blockId = blockIdTracker.getAndIncrement();
                    Block forEachBlock = Block.idAndText("" + (blockId), block.text()+"iteration(" + forEachCounter.getAndIncrement()+"): "+devcie);
                    updatedRepresentationBuilder.parentToChild(block, forEachBlock);
                    BiConsumer<In, Out> newRunnable = (in, out) -> {
                        MissionInput scopedInput = MissionInput.from(params).and(configuration.itemPlaceholder().name(),
                                devcie);
                        configuration.runnable().accept(scopedInput, out);

                    };
                    updatedRunnablesBuilder.put(forEachBlock, newRunnable);
                });

            });
        }
        final Map<Block, BiConsumer<In, Out>> updatedRunnables = updatedRunnablesBuilder.build();
        ImmutableMissionRepresentation.Builder newRepresentationBuilder = ImmutableMissionRepresentation.builder(runnableLeafMission.treeStructure().rootBlock());
        Map<Block, MissionInput> scopedInputs = new HashMap<>();
        Map<Block, Placeholder<?>> forEachBlocks = runnableLeafMission.getForEachBlocks();
        final ImmutableMap.Builder<Block, BiConsumer<In, Out>> updatedRunnablesAfterTraverseBuilder = ImmutableMap.builder();
        traverse(runnableLeafMission, runnableLeafMission.treeStructure().missionRepresentation().rootBlock(), input, "", newRepresentationBuilder, scopedInputs, updatedRunnablesAfterTraverseBuilder);
        System.out.println(newRepresentationBuilder.build().parentsToChildren());
        
        Map<Block, Map<String, Object>> blockInputs = new HashMap<>();
        forEachBlocks.forEach((forEachBlock, placeholder)->{
            Collection<?> forEachItems = (Collection<?>)input.get(forEachBlocks.get(forEachBlock));
            forEachItems.forEach(item -> {
            	ForEachConfiguration<?,?> forEachConfig = runnableLeafMission.getForEachBlocksConfigurations().get(forEachBlock);
            	System.out.println(forEachConfig.itemPlaceholder());
            	System.out.println(item);
//            	MissionInput.from(params).and(key, value)
            	TreeStructure subTree = runnableLeafMission.treeStructure().substructure(forEachBlock, item.toString());
            	//input for all leafs in subtree must be extended by itemPlaceholder 
            	for(Block block : subTree.allBlocks()) {
            		if(blockInputs.containsKey(block)) {
            			blockInputs.get(block).put(forEachConfig.itemPlaceholder().name(), item);
            		}
            		else {
            			blockInputs.put(block, new HashMap<>());
            			blockInputs.get(block).put(forEachConfig.itemPlaceholder().name(), item);
            		}
            	}
            	System.out.println("p2C"+subTree.missionRepresentation().parentsToChildren());
                System.out.println(subTree.allBlocks());
                System.out.println(subTree.missionRepresentation().parentsToChildren());
            });
        });

        System.out.println(blockInputs);

        //TreeStructure updatedTreeStructure = new TreeStructure(updatedRepresentationBuilder.build(), runnableLeafMission.treeStructure().parallelBlocks());
        //TreeTracker<Result> resultTracker = TreeTracker.create(updatedTreeStructure.missionRepresentation(), Result.UNDEFINED, Result::summaryOf);
        //TreeTracker<RunState> runStateTracker = TreeTracker.create(updatedTreeStructure.missionRepresentation(), RunState.UNDEFINED, RunState::summaryOf);
        TreeStructure updatedTreeStructure = new TreeStructure(newRepresentationBuilder.build(), ImmutableSet.of());
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

        Function<In, ?> contextFactory = mission.contextFactory();
        if (contextFactory == null) {
            return in;
        }
        return in.and(Placeholders.context().name(), contextFactory.apply(in));
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
