package io.molr.mole.core.support;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionState;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.api.Mole;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Should be very similar to an OngoingMissionRun (probably inherit or delegate from it), however, the return type is already fixed....
 *
 * @param <R> the returntype of the mission
 */
public class OngoingReturningMissionRun<R> extends OngoingMissionRun {

    private Placeholder<R> returnType;

    public OngoingReturningMissionRun(Mole mole, Mono<MissionHandle> handle, Placeholder<R> returnType) {
        super(mole, handle);
        this.returnType = returnType;
    }

    @Override
    public OngoingReturningMissionRun<R> and() {
        return this;
    }

    public R awaitOuputValue() {
        return returnOutput().whenFinished();
    }

    public ReturnHelper<R> returnOutput() {
        return new ReturnHelper<R>(new ReturnOutput());
    }

    private class ReturnOutput implements Function<MissionState, R> {
        @Override
        public R apply(MissionState missionState) {
            return handle
                    .flatMapMany(mole::outputsFor)
                    .blockFirst()
                    .get(getRootBlock(), returnType);
        }

        private Block getRootBlock() {
            return handle
                    .flatMapMany(mole::representationsFor)
                    .elementAt(0)
                    .block()
                    .rootBlock();
        }
    }
}
