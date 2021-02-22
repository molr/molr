package io.molr.mole.core.tree;

import io.molr.commons.domain.*;
import io.molr.mole.core.tree.exception.MissionDisposeException;
import io.molr.mole.core.tree.executor.StrandExecutorFactory;
import io.molr.mole.core.tree.tracking.Tracker;
import io.molr.mole.core.tree.tracking.TreeTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.molr.commons.domain.StrandCommand.STEP_INTO;


/**
 * Keeps track of the state of the execution of one mission instance. It assumes a tree of execution blocks, can execute
 * them and keeps track of cursor positions within strands.
 */
public class TreeMissionExecutor implements MissionExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeMissionExecutor.class);
    
    private final Flux<MissionState> states;
    private final StrandExecutorFactory strandExecutorFactory;
    private final MissionOutputCollector outputCollector;
    private final Tracker<Result> resultTracker;
    private final MissionRepresentation representation;
    private final Set<Block> breakpoints;
    private final Set<Block> blocksToBeIgnored;
    private final EmitterProcessor<Object> statesSink;
    private final TreeNodeStates nodeStates;
    private final StrandExecutor rootExecutor;
    
	public TreeMissionExecutor(TreeStructure treeStructure, LeafExecutor leafExecutor, Tracker<Result> resultTracker,
			MissionOutputCollector outputCollector,
			TreeTracker<RunState> runStateTracker, ExecutionStrategy executionStrategy) {
        this.breakpoints = ConcurrentHashMap.newKeySet();
        this.blocksToBeIgnored = ConcurrentHashMap.newKeySet();
        treeStructure.missionRepresentation().blockAttributes().forEach((block, attribute)->{
        	if(attribute==BlockAttribute.IGNORE) {
        		this.blocksToBeIgnored.add(block);
        	}
        	else if(attribute == BlockAttribute.BREAK) {
        		this.breakpoints.add(block);
        	}
        });
        this.nodeStates = new TreeNodeStates(treeStructure);
		strandExecutorFactory = new StrandExecutorFactory(leafExecutor, nodeStates);
        this.outputCollector = outputCollector;
        this.resultTracker = resultTracker;
        this.representation = treeStructure.missionRepresentation();


        //generate a signal for each update in block stream and state stream of all executors and create a flux that is gathering mission states on that events
        statesSink = EmitterProcessor.create();
    	runStateTracker.updatedBlocksStream().subscribe(any -> {
    		statesSink.onNext(new Object());
    	});
        strandExecutorFactory.newStrandsStream().subscribe(newExecutor -> {
        	newExecutor.getBlockStream().subscribe(any -> statesSink.onNext(new Object()));
            newExecutor.getStateStream().subscribe(any -> {statesSink.onNext(new Object());},
                error->{LOGGER.info("States Stream of strand executor finished with error", error);}, ()->{});
        });
        Scheduler statesSinkScheduler = Schedulers.elastic();
        states = statesSink.map(signal -> gatherMissionState())
                .cache(1)
                .sample(Duration.ofMillis(100))
                .publishOn(statesSinkScheduler)
                .doFinally(signal -> {
                	statesSinkScheduler.dispose();
                });

        rootExecutor = strandExecutorFactory.createRootStrandExecutor(treeStructure, breakpoints, blocksToBeIgnored, executionStrategy);
        rootExecutor.getStateStream().subscribe(next->{},error->onRootExecutorStatesStreamComplete(), this::onRootExecutorStatesStreamComplete);

        if (!treeStructure.isLeaf(treeStructure.rootBlock())) {
            rootExecutor.instruct(STEP_INTO);
        }
        states.subscribe();
    }

    private void onRootExecutorStatesStreamComplete() {
    	outputCollector.onComplete();
        statesSink.onComplete();
    }

    @Override
    public Flux<MissionState> states() {
        return states;
    }

    @Override
    public Flux<MissionOutput> outputs() {
        return outputCollector.asStream();
    }

    @Override
    public Flux<MissionRepresentation> representations() {
        return Flux.just(this.representation);
    }

    /*
     * TODO optimize
     * - the gathering itself
     * - and maybe the way how and how often it is triggered by StrandExecutors
     */
    private MissionState gatherMissionState() {
        Result rootResult = resultTracker.resultFor(representation.rootBlock());
        MissionState.Builder builder = MissionState.builder(rootResult);
        for (StrandExecutor executor : strandExecutorFactory.allStrandExecutors()) {
            RunState runState = executor.getActualState();
            Block cursor = executor.getActualBlock();
            Optional<Strand> parent = strandExecutorFactory.parentOf(executor.getStrand());
            if (parent.isPresent()) {
                builder.add(executor.getStrand(), runState, cursor, parent.get(), executor.getAllowedCommands());
            } else {
                builder.add(executor.getStrand(), runState, cursor, executor.getAllowedCommands());
            }
        }

        nodeStates.getResultStates().getSnapshot().forEach((block, state) -> {builder.blockResult(block, state);});
        nodeStates.getRunStates().getSnapshot().forEach((block, state)-> {builder.blockRunState(block, state);});
        
        breakpoints.forEach(builder::addBreakpoint);
        blocksToBeIgnored.forEach(builder::addIgnoreBlock);
        representation.allBlocks().forEach(block -> {
            boolean isBreakpoint = breakpoints.contains(block);
            if(isBreakpoint) {
                builder.addAllowedCommand(block, BlockCommand.UNSET_BREAKPOINT);
            }
            else {
                builder.addAllowedCommand(block, BlockCommand.SET_BREAKPOINT);                
            }
            boolean ignore = blocksToBeIgnored.contains(block);
            if(ignore)
            {
            	builder.addAllowedCommand(block, BlockCommand.UNSET_IGNORE);
            }
            else {
            	builder.addAllowedCommand(block, BlockCommand.SET_IGNORE);
            }
        });

        if(isDisposable()) {
            builder.addAllowedCommand(MissionCommand.DISPOSE);
        }
        MissionState missionState = builder.build();
        return missionState;
    }

    @Override
    public void instruct(Strand strand, StrandCommand command) {
        StrandExecutor executor = strandExecutorFactory.getStrandExecutorFor(strand);
        executor.instruct(command);
    }

    @Override
    public void instructRoot(StrandCommand command) {
        instruct(strandExecutorFactory.rootStrand(), command);
    }

    @Override
    public void instructBlock(String blockId, BlockCommand command) {
        
        LOGGER.info(command.name()+" for block="+blockId);
        
        if(command == BlockCommand.UNSET_BREAKPOINT) {
            boolean breakpointRemoved = breakpoints.removeIf(block -> block.id().equals(blockId));
            if(breakpointRemoved) {
                statesSink.onNext(new Object());
            }
        } else if(command == BlockCommand.SET_BREAKPOINT){
            Block block = representation.blockOfId(blockId).get();
            boolean breakpointAdded = breakpoints.add(block);
            if(breakpointAdded) {
                statesSink.onNext(new Object());
            }
        }
        if(command == BlockCommand.UNSET_IGNORE) {
            boolean blockRemoved = blocksToBeIgnored.removeIf(block -> block.id().equals(blockId));
            if(blockRemoved) {
                statesSink.onNext(new Object());
            }
        } else if(command == BlockCommand.SET_IGNORE){
            Block block = representation.blockOfId(blockId).get();
            boolean added = blocksToBeIgnored.add(block);
            if(added) {
                statesSink.onNext(new Object());
            }
        }    
    }

    @Override
    public void dispose() {
        if(!isDisposable()) {
            throw new MissionDisposeException();            
        }
        statesSink.onComplete();
        outputCollector.onComplete();
    }
    
    private boolean isDisposable() {
    	return rootExecutor.isComplete();
    }
    
    public void abort() {
//        strandExecutorFactory.getStrandExecutorFor(getRootStrand()).
    }
}
