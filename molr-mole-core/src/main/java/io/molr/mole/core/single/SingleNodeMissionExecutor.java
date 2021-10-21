package io.molr.mole.core.single;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.BlockCommand;
import io.molr.commons.domain.MissionInput;
import io.molr.commons.domain.MissionOutput;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.MissionState;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.Placeholders;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.Strand;
import io.molr.commons.domain.StrandCommand;
import io.molr.commons.util.Exceptions;
import io.molr.mole.core.tree.BlockOutputCollector;
import io.molr.mole.core.tree.ConcurrentMissionOutputCollector;
import io.molr.mole.core.tree.MissionExecutor;
import io.molr.mole.core.tree.MissionOutputCollector;
import io.molr.mole.core.tree.exception.MissionDisposeException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

public class SingleNodeMissionExecutor<R> implements MissionExecutor {

    private final static Logger LOGGER = LoggerFactory.getLogger(SingleNodeMissionExecutor.class);

    private final SingleNodeMission<R> mission;
    private final MissionRepresentation representation;
    private final MissionOutputCollector outputCollector = new ConcurrentMissionOutputCollector();
    private final BlockOutputCollector output;

    private final ReplayProcessor<MissionState> stateSink = ReplayProcessor.cacheLast();
    private final Flux<MissionState> stateStream = stateSink.publishOn(Schedulers.newSingle("MissionState publisher"));

    private final ReplayProcessor<MissionRepresentation> representations = ReplayProcessor.cacheLast();
    private final MissionInput input;

    private final Strand singleStrand = Strand.ofId("0");
    private final AtomicReference<RunState> strandRunState = new AtomicReference<>(RunState.PAUSED);
    private final AtomicReference<Result> result = new AtomicReference<>(Result.UNDEFINED);
    private final AtomicBoolean started = new AtomicBoolean(false);

    private final ExecutorService executorService;


    public SingleNodeMissionExecutor(SingleNodeMission<R> mission, Map<String, Object> parameters) {
        this.mission = requireNonNull(mission, "mission must not be null");
        executorService = Executors.newSingleThreadExecutor();
        this.input = MissionInput.from(parameters);
        this.representation = SingleNodeMissions.representationFor(mission);
        this.representations.onNext(this.representation);
        this.output = new BlockOutputCollector(outputCollector, representation.rootBlock());
        publishState();
    }

    private void resume() {
        if (started.getAndSet(true)) {
            LOGGER.warn("Already Running. Doing nothing.");
            return;
        }
        this.strandRunState.set(RunState.RUNNING);
        executorService.submit(() -> {
            execute();
            finish();
        });
        publishState();
    }


    private void execute() {
        try {
            R returnValue = mission.executable().apply(input, output);
            result.set(Result.SUCCESS);
            emitReturnValue(returnValue);
        } catch (Exception e) {
            result.set(Result.FAILED);
            LOGGER.warn("Mission {} failed with exception. ", mission, e);
            output.emit(Placeholders.THROWN, Exceptions.stackTraceFrom(e));
        }
    }

    private void emitReturnValue(R returnValue) {
        Class<R> returnType = mission.returnType();
        if (Void.class.isAssignableFrom(returnType)) {
            LOGGER.debug("Mission {} has a void return type. Emitting no return value.");
            return;
        }
        Optional<Placeholder<R>> placeholder = Placeholders.returned(returnType);
        if (placeholder.isPresent()) {
            output.emit(placeholder.get(), returnValue);
        } else {
            LOGGER.debug("Placeholder for type {} is not supported. Emitting the string representation of the return value instead.");
            output.emit(Placeholders.RETURNED_STRING, Objects.toString(returnValue));
        }
    }


    private void finish() {
        this.strandRunState.set(RunState.FINISHED);
        publishState();
    }

    private void publishState() {
        Result rootResult = this.result.get();
        RunState rootRunState = strandRunState.get();

        MissionState.Builder builder = MissionState.builder(rootResult);
        builder.add(singleStrand, rootRunState, cursor(), allowedCommands());
        builder.blockRunState(rootBlock(), rootRunState);
        builder.blockResult(rootBlock(), rootResult);
        stateSink.onNext(builder.build());
    }


    private Block cursor() {
        if (RunState.FINISHED.equals(strandRunState.get())) {
            return null;
        }
        return rootBlock();
    }

    private Block rootBlock() {
        return representation.rootBlock();
    }

    private Set<StrandCommand> allowedCommands() {
        if (!started.get()) {
            return ImmutableSet.of(StrandCommand.RESUME);
        }
        return ImmutableSet.of();
    }


    @Override
    public void instruct(Strand strand, StrandCommand command) {
        if (!singleStrand.equals(strand)) {
            LOGGER.warn("given strand {} is not equal to strand {}. Doing nothing.", strand, singleStrand);
            return;
        }
        if (StrandCommand.RESUME.equals(command)) {
            resume();
        } else {
            LOGGER.warn("given command {} is not supported. Doing nothing.", command);
        }
    }

    @Override
    public void instructRoot(StrandCommand command) {
        instruct(singleStrand, command);
    }


    @Override
    public Flux<MissionState> states() {
        return stateStream;
    }
    
    @Override
    public void instructBlock(String blockID, BlockCommand command) {
        throw new IllegalStateException("Block commands not supported by SingleNodeMissionExectuor");
    }

    @Override
    public Flux<MissionOutput> outputs() {
        return outputCollector.asStream();
    }

    @Override
    public Flux<MissionRepresentation> representations() {
        return this.representations;
    }

    @Override
    public void dispose() {
        if(this.result.get().equals(Result.UNDEFINED)) {
            throw new MissionDisposeException();
        }
        stateSink.onComplete();
    }

}
