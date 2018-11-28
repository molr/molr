package org.molr.mole.core.tree;

import org.molr.commons.domain.*;
import org.molr.mole.core.tree.tracking.Tracker;
import org.molr.mole.core.tree.tracking.TreeTracker;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Optional;

import static org.molr.commons.domain.StrandCommand.STEP_INTO;


/**
 * Keeps track of the state of the execution of one mission instance. It assumes a tree of execution blocks, can execute
 * them and keeps track of cursor positions within strands.
 */
public class TreeMissionExecutor implements MissionExecutor {

    private final Flux<MissionState> states;
    private final StrandFactoryImpl strandFactory;
    private final StrandExecutorFactory strandExecutorFactory;
    private final MissionOutputCollector outputCollector;
    private final Tracker<Result> resultTracker;
    private final TreeTracker<RunState> runStateTracker;
    private final MissionRepresentation representation;

    public TreeMissionExecutor(TreeStructure treeStructure, LeafExecutor leafExecutor, Tracker<Result> resultTracker, MissionOutputCollector outputCollector, TreeTracker<RunState> runStateTracker) {
        this.runStateTracker = runStateTracker;
        strandFactory = new StrandFactoryImpl();
        strandExecutorFactory = new StrandExecutorFactory(strandFactory, leafExecutor);
        this.outputCollector = outputCollector;
        this.resultTracker = resultTracker;
        this.representation = treeStructure.missionRepresentation();

        EmitterProcessor<Object> statesSink = EmitterProcessor.create();
        strandExecutorFactory.newStrandsStream().subscribe(newExecutor -> {
            newExecutor.getBlockStream().subscribe(any -> statesSink.onNext(new Object()));
            newExecutor.getStateStream().subscribe(any -> statesSink.onNext(new Object()));
        });
        states = statesSink.map(signal -> gatherMissionState())
                .cache(1)
                .sample(Duration.ofMillis(100))
                .publishOn(Schedulers.elastic());

        Strand rootStrand = strandFactory.rootStrand();
        StrandExecutor rootExecutor = strandExecutorFactory.createStrandExecutor(rootStrand, treeStructure);

        if (!treeStructure.isLeaf(treeStructure.rootBlock())) {
            rootExecutor.instruct(STEP_INTO);
        }
    }

    @Deprecated
    public Strand getRootStrand() {
        return strandFactory.rootStrand();
    }

    @Deprecated
    public StrandFactoryImpl getStrandFactory() {
        return strandFactory;
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

    private MissionState gatherMissionState() {
        Result rootResult = resultTracker.resultFor(representation.rootBlock());
        MissionState.Builder builder = MissionState.builder(rootResult);
        for (StrandExecutor executor : strandExecutorFactory.allStrandExecutors()) {
            RunState runState = executor.getActualState();
            Block cursor = executor.getActualBlock();
            Optional<Strand> parent = strandFactory.parentOf(executor.getStrand());
            if (parent.isPresent()) {
                builder.add(executor.getStrand(), runState, cursor, parent.get(), executor.getAllowedCommands());
            } else {
                builder.add(executor.getStrand(), runState, cursor, executor.getAllowedCommands());
            }
        }

        resultTracker.blockResults().entrySet().forEach(e -> builder.blockResult(e.getKey(), e.getValue()));
        runStateTracker.blockResults().entrySet().forEach(e -> builder.blockRunState(e.getKey(), e.getValue()));
        return builder.build();
    }

    @Override
    public void instruct(Strand strand, StrandCommand command) {
        StrandExecutor executor = strandExecutorFactory.getStrandExecutorFor(strand);
        executor.instruct(command);
    }

    @Override
    public void instructRoot(StrandCommand command) {
        instruct(strandFactory.rootStrand(), command);
    }


}
