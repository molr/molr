package io.molr.mole.core.support;

import static io.molr.commons.domain.Placeholders.returned;

import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionState;
import io.molr.commons.domain.RunState;
import io.molr.mole.core.api.Mole;
import reactor.core.publisher.Mono;

/**
 * Provides capabilities to control an ongoing mission run with type safety
 *
 * @param <R> the returntype of the mission
 */
public class OngoingReturningMissionRun<R> extends OngoingMissionRun {

    private final Class<R> returnType;

    /**
     * @param mole       the {@link Mole} which has missions registered
     * @param handle     the {@link MissionHandle} of running {@link Mission}
     * @param returnType the type for return value of {@link Mission}
     */
    public OngoingReturningMissionRun(Mole mole, Mono<MissionHandle> handle, Class<R> returnType) {
        super(mole, handle);
        this.returnType = returnType;
    }

    /**
     * Gives meaning to method chaining when chaining two unrelated methods
     *
     * @return an instance of {@link OngoingReturningMissionRun}
     */
    @Override
    public OngoingReturningMissionRun<R> and() {
        return this;
    }

    /**
     * @return the output {@link R} of {@link Mission} when {@link RunState} is FINISHED
     */
    public R awaitOuputValue() {
        return returnOutput().whenFinished();
    }

    /**
     * @return the {@link ReturnHelper} to get output {@link R}
     */
    public ReturnHelper<R> returnOutput() {
        return new ReturnHelper<R>(new ReturnOutput());
    }

    /**
     * A functions that returns the output {@link R} of {@link Mission}
     */
    private class ReturnOutput implements Function<MissionState, R> {
        @Override
        public R apply(MissionState missionState) {
            return asyncHandle()
                    .flatMapMany(mole()::outputsFor)
                    .blockFirst()
                    .get(getRootBlock(), returned(returnType).get());
        }

        private Block getRootBlock() {
            return asyncHandle()
                    .flatMapMany(mole()::representationsFor)
                    .elementAt(0)
                    .block()
                    .rootBlock();
        }
    }
}
