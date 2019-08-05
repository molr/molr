package io.molr.mole.core.support;

import io.molr.commons.domain.Mission;
import io.molr.mole.core.support.domain.*;


/**
 * Methods required by {@link MissionControlSupport} to trigger missions using stubs.
 *
 * @author HimanshuSahu31
 */
public interface MissionStubSupport {

    /**
     * Initiates the mission represented by the stub and returns control to the running {@link Mission}
     *
     * @param stub the {@link VoidStub0}
     * @return control to the running {@link Mission}
     */
    public OngoingMissionRun start(VoidStub0 stub);

    /**
     * Initiates the mission represented by the stub and returns control to the running {@link Mission}
     *
     * @param stub the {@link MissionStub0}
     * @param <R>  the return type of {@link Mission}
     * @return control to the running {@link Mission}
     */
    <R> OngoingReturningMissionRun<R> start(MissionStub0<R> stub);

    /**
     * Initiates the mission represented by the stub and returns control to the running {@link Mission}
     *
     * @param stub the {@link VoidStub1}
     * @param p1   the parameter 1 of {@link Mission}
     * @param <P1> the type of parameter 1
     * @return control to the running {@link Mission}
     */
    public <P1> OngoingMissionRun start(VoidStub1<P1> stub, P1 p1);

    /**
     * Initiates the mission represented by the stub and returns control to the running {@link Mission}
     *
     * @param stub the {@link MissionStub1}
     * @param p1   the parameter 1 of {@link Mission}
     * @param <P1> the type of parameter 1
     * @param <R>  the return type of {@link Mission}
     * @return control to the running {@link Mission}
     */
    <P1, R> OngoingReturningMissionRun<R> start(MissionStub1<P1, R> stub, P1 p1);

    /**
     * Initiates the mission represented by the stub and returns control to the running {@link Mission}
     *
     * @param stub the {@link VoidStub2}
     * @param p1   the parameter 1 of {@link Mission}
     * @param p2   the parameter 2 of {@link Mission}
     * @param <P1> the type of parameter 1
     * @param <P2> the type of parameter 2
     * @return control to the running {@link Mission}
     */
    public <P1, P2> OngoingMissionRun start(VoidStub2<P1, P2> stub, P1 p1, P2 p2);

    /**
     * Initiates the mission represented by the stub and returns control to the running {@link Mission}
     *
     * @param stub the {@link MissionStub2}
     * @param p1   the parameter 1 of {@link Mission}
     * @param p2   the parameter 2 of {@link Mission}
     * @param <P1> the type of parameter 1
     * @param <P2> the type of parameter 2
     * @param <R>  the return type of {@link Mission}
     * @return control to the running {@link Mission}
     */
    <P1, P2, R> OngoingReturningMissionRun<R> start(MissionStub2<P1, P2, R> stub, P1 p1, P2 p2);

    /*z
    ... probably up to 5?
     */
}
