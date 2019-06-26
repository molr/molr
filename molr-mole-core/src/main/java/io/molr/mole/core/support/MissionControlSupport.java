package io.molr.mole.core.support;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.support.domain.MissionStub0;
import io.molr.mole.core.support.domain.MissionStub1;
import io.molr.mole.core.support.domain.MissionStub2;
import reactor.core.publisher.Mono;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Provides convenience methods for simple ways of running missions. It might have to be seen later, if some of them
 * should be moved later to the agency directly, especially if this move would prevent unnecessary network traffic.
 */
public class MissionControlSupport implements MissionStubSupport {

    private final Mole mole;

    private MissionControlSupport(Mole mole) {
        this.mole = requireNonNull(mole, "mole must not be null");
    }

    public static MissionControlSupport from(Mole mole) {
        return new MissionControlSupport(mole);
    }

    public OngoingMissionRun start(Mission mission, Map<String, Object> missionParameters) {
        Mono<MissionHandle> handle = mole.instantiate(mission, missionParameters);
        handle.subscribe(h -> mole.instructRoot(h, StrandCommand.RESUME));
        return new OngoingMissionRun(mole, handle);
    }

    public OngoingMissionRun start(String missionName, Map<String, Object> missionParameters) {
        return start(new Mission(missionName), missionParameters);
    }

    public OngoingMissionRun control(Mono<MissionHandle> handle) {
        return new OngoingMissionRun(mole, handle);
    }

    public OngoingMissionRun control(MissionHandle handle) {
        return control(Mono.just(handle));
    }

    @Override
    public <R> OngoingReturningMissionRun<R> start(MissionStub0<R> stub) {
        Mono<MissionHandle> handle = mole.instantiate(stub.getMission(), stub.getParametersMap());
        handle.subscribe(h -> mole.instructRoot(h, StrandCommand.RESUME));
        return new OngoingReturningMissionRun<R>(mole, handle, stub.getReturnType());
    }

    @Override
    public <P1, R> OngoingReturningMissionRun<R> start(MissionStub1<P1, R> stub, P1 p1) {
        Mono<MissionHandle> handle = mole.instantiate(stub.getMission(), stub.getParametersMap(p1));
        handle.subscribe(h -> mole.instructRoot(h, StrandCommand.RESUME));
        return new OngoingReturningMissionRun<R>(mole, handle, stub.getReturnType());
    }

    @Override
    public <P1, P2, R> OngoingReturningMissionRun<R> start(MissionStub2<P1, P2, R> stub, P1 p1, P2 p2) {
        Mono<MissionHandle> handle = mole.instantiate(stub.getMission(), stub.getParametersMap(p1, p2));
        handle.subscribe(h -> mole.instructRoot(h, StrandCommand.RESUME));
        return new OngoingReturningMissionRun<R>(mole, handle, stub.getReturnType());
    }
}
