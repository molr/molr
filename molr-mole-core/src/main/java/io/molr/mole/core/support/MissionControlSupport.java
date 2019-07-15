package io.molr.mole.core.support;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.support.domain.*;
import reactor.core.publisher.Mono;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Provides convenience methods for simple ways of running missions. It might have to be seen later, if some of them
 * should be moved later to the agency directly, especially if this move would prevent unnecessary network traffic.
 */
public class MissionControlSupport implements MissionStubSupport {

    private final Mole mole;

    /**
     * Constructs a {@link MissionControlSupport} with the specified {@link Mole}
     *
     * @param mole the {@link Mole} which has missions registered
     */
    private MissionControlSupport(Mole mole) {
        this.mole = requireNonNull(mole, "mole must not be null");
    }

    /**
     * @param mole the {@link Mole}
     * @return an instance of {@link MissionControlSupport} with specified {@link Mole}
     */
    public static MissionControlSupport from(Mole mole) {
        return new MissionControlSupport(mole);
    }

    /**
     * @param mission           the {@link Mission} which has been registered with the {@link Mole}
     * @param missionParameters the mission parameters required by the {@link Mission} during it's execution
     * @return
     */
    public OngoingMissionRun start(Mission mission, Map<String, Object> missionParameters) {
        Mono<MissionHandle> handle = mole.instantiate(mission, missionParameters);
        handle.subscribe(h -> mole.instructRoot(h, StrandCommand.RESUME));
        return new OngoingMissionRun(mole, handle);
    }

    /**
     * @param missionName
     * @param missionParameters the mission parameters required by the {@link Mission} during it's execution
     * @return
     */
    public OngoingMissionRun start(String missionName, Map<String, Object> missionParameters) {
        return start(new Mission(missionName), missionParameters);
    }

    /**
     * @param handle the {@link MissionHandle} to the running {@link Mission}
     * @return control to the running {@link Mission} represented by the argument
     */
    public OngoingMissionRun control(Mono<MissionHandle> handle) {
        return new OngoingMissionRun(mole, handle);
    }

    /**
     * @param handle the {@link MissionHandle} to the running {@link Mission}
     * @return control to the running {@link Mission} represented by the argument
     */
    public OngoingMissionRun control(MissionHandle handle) {
        return control(Mono.just(handle));
    }

    @Override
    public OngoingMissionRun start(VoidStub0 stub) {
        return start(stub.mission(), stub.parameters());
    }

    @Override
    public <R> OngoingReturningMissionRun<R> start(MissionStub0<R> stub) {
        OngoingMissionRun ongoingMissionRun = start(stub.mission(), stub.parameters());
        return new OngoingReturningMissionRun<R>(ongoingMissionRun.mole(), ongoingMissionRun.asyncHandle(),
                stub.returnType());
    }

    public <P1> OngoingMissionRun start(VoidStub1<P1> stub, P1 p1) {
        return start(stub.mission(), stub.parameters(p1));
    }

    @Override
    public <P1, R> OngoingReturningMissionRun<R> start(MissionStub1<P1, R> stub, P1 p1) {
        OngoingMissionRun ongoingMissionRun = start(stub.mission(), stub.parameters(p1));
        return new OngoingReturningMissionRun<R>(ongoingMissionRun.mole(), ongoingMissionRun.asyncHandle(),
                stub.returnType());
    }

    public <P1, P2> OngoingMissionRun start(VoidStub2<P1, P2> stub, P1 p1, P2 p2) {
        return start(stub.mission(), stub.parameters(p1, p2));
    }

    @Override
    public <P1, P2, R> OngoingReturningMissionRun<R> start(MissionStub2<P1, P2, R> stub, P1 p1, P2 p2) {
        OngoingMissionRun ongoingMissionRun = start(stub.mission(), stub.parameters(p1, p2));
        return new OngoingReturningMissionRun<R>(ongoingMissionRun.mole(), ongoingMissionRun.asyncHandle(),
                stub.returnType());
    }
}
