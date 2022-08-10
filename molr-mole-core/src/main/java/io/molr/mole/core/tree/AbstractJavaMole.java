package io.molr.mole.core.tree;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.AgencyState;
import io.molr.commons.domain.AtomicIncrementMissionHandleFactory;
import io.molr.commons.domain.BlockCommand;
import io.molr.commons.domain.ImmutableAgencyState;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionCommand;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionHandleFactory;
import io.molr.commons.domain.MissionInstance;
import io.molr.commons.domain.MissionOutput;
import io.molr.commons.domain.MissionParameterDescription;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.MissionState;
import io.molr.commons.domain.Strand;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.tree.exception.MissionDisposeException;
import io.molr.mole.core.utils.ThreadFactories;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.scheduler.Schedulers;

public abstract class AbstractJavaMole implements Mole {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractJavaMole.class);
    
    private final Sinks.Many<AgencyState> statesSink = Sinks.many().replay().latest();
    private final Flux<AgencyState> statesStream = statesSink.asFlux().publishOn(Schedulers.boundedElastic());

    private final Map<MissionHandle, MissionExecutor> executors = new ConcurrentHashMap<>();
    private final Map<MissionHandle, Map<String, Object>> missionInputs = new ConcurrentHashMap<>();
    private final Set<MissionInstance> instances = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final MissionHandleFactory handleFactory = new AtomicIncrementMissionHandleFactory(this);
    private final ExecutorService moleExecutor = newSingleThreadExecutor(ThreadFactories.namedDaemonThreadFactory("java-mole-%d"));

    private final Set<Mission> availableMissions;

    protected AbstractJavaMole(Set<Mission> availableMissions) {
        this.availableMissions = availableMissions;
        publishState();
    }

    @Override
    public Mono<MissionHandle> instantiate(Mission mission, Map<String, Object> params) {
        return supplyAsync(() -> {
            validateParameterValues(mission, params);
            MissionHandle handle = handleFactory.createHandle();
            executors.put(handle, executorFor(mission, params));
            missionInputs.put(handle, params);
            instances.add(new MissionInstance(handle, mission));
            publishState();
            return handle;
        });
    }

    private void validateParameterValues(Mission mission, Map<String, Object> params) {
        MissionParameterDescription parameterDescription = missionParameterDescriptionOf(mission);
        parameterDescription.parameters().forEach(parameter -> {
            String parameterName = parameter.placeholder().name();
            Object parameterValue = params.get(parameterName);
            if(parameterValue == null && !parameter.isRequired()) {
                /**
                 * no need to check optional parameter values if no value has been specified
                 */
                return;
            }

            if(parameterValue==null) {
                throw new IllegalArgumentException("missing mandatory parameter "+parameter.placeholder().name());
            }

            if(Collection.class.isAssignableFrom(parameterValue.getClass())) {
            	LOGGER.warn("Validation of collection values has been skipped.");
            	return;
            }
            
            if(parameter.allowedValues() != null && !parameter.allowedValues().isEmpty()) {
                if(!parameter.allowedValues().contains(parameterValue)) {
                    throw new IllegalArgumentException("Cannot instantiate mission: Value "+parameterValue
                            +" is not allowed for parameter "+parameterName);
                }
            }
        });
    }
        
    @Override
    public Mono<MissionParameterDescription> parameterDescriptionOf(Mission mission) {
        return supplyAsync(() -> missionParameterDescriptionOf(mission));
    }

    @Override
    public Flux<AgencyState> states() {
        return this.statesStream;
    }

    @Override
    public final Flux<MissionState> statesFor(MissionHandle handle) {
        return fromExecutorOrError(handle, MissionExecutor::states);
    }

    @Override
    public final Flux<MissionOutput> outputsFor(MissionHandle handle) {
        return fromExecutorOrError(handle, MissionExecutor::outputs);
    }

    @Override
    public final Flux<MissionRepresentation> representationsFor(MissionHandle handle) {
        return fromExecutorOrError(handle, MissionExecutor::representations);
    }

    @Override
    public Mono<Map<String, Object>> inputFor(MissionHandle handle) {
    	try {
    		Map<String, Object> input = missionInputs.get(handle);
    		return Mono.just(input);
    	}
    	catch(Exception e) {
    		return Mono.error(e);
    	}
    }
    
    @Override
    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return supplyAsync(() -> missionRepresentationOf(mission));
    }

    private <T> Flux<T> fromExecutorOrError(MissionHandle handle, Function<MissionExecutor, Flux<T>> mapper) {
        return Optional.ofNullable(executors.get(handle))
                .map(mapper)
                .orElse(Flux.error(new IllegalStateException("No executor for handle '" + handle + "'")));
    }

    @Override
    public final void instruct(MissionHandle handle, Strand strand, StrandCommand command) {
        Optional.ofNullable(executors.get(handle))
                .ifPresent(e -> e.instruct(strand, command));
    }

    @Override
    public final void instructRoot(MissionHandle handle, StrandCommand command) {
        Optional.ofNullable(executors.get(handle))
                .ifPresent(e -> e.instructRoot(command));
    }
    
    @Override
    public void instruct(MissionHandle handle, MissionCommand command) {
        if(command.equals(MissionCommand.DISPOSE)) {
            moleExecutor.submit(()->{
                Optional.ofNullable(executors.get(handle)).ifPresent(e -> {
                    try{
                        e.dispose();
                        executors.remove(handle);
                        missionInputs.remove(handle);
                        instances.removeIf(missionInstance -> {
                            return missionInstance.handle().equals(handle);
                        });
                        publishState();
                        LOGGER.debug("Successfully disposed mission instance: "+handle.id());
                    }
                    catch(MissionDisposeException missionDisposeException) {
                        LOGGER.error("Error while trying to dispose mission instance: "+handle.id());
                    }
                });
            });
        }
    }
    
    @Override
    public final void instructBlock(MissionHandle handle, String blockId, BlockCommand command) {
        Optional.ofNullable(executors.get(handle)).ifPresent(e-> e.instructBlock(blockId, command));
    }

    private <T> Mono<T> supplyAsync(Supplier<T> supplier) {
        return Mono.fromFuture(CompletableFuture.supplyAsync(supplier, moleExecutor));
    }

    private void publishState() {
        EmitResult result = statesSink.tryEmitNext(ImmutableAgencyState.of(ImmutableSet.copyOf(availableMissions), ImmutableList.copyOf(instances)));
        result.orThrow();
    }

    protected abstract MissionExecutor executorFor(Mission mission, Map<String, Object> params);

    protected abstract MissionRepresentation missionRepresentationOf(Mission mission);

    protected abstract MissionParameterDescription missionParameterDescriptionOf(Mission mission);
}
