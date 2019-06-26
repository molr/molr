package io.molr.mole.core.support;

import io.molr.mole.core.support.domain.MissionStub0;
import io.molr.mole.core.support.domain.MissionStub1;
import io.molr.mole.core.support.domain.MissionStub2;


/*
The methods in here might be directly part of MissionControlSupport ... might simplify things...
 */
public interface MissionStubSupport {

    <R> OngoingReturningMissionRun<R> start(MissionStub0<R> stub);

    <P1, R> OngoingReturningMissionRun<R> start(MissionStub1<P1, R> stub, P1 p1);

    <P1, P2, R> OngoingReturningMissionRun<R> start(MissionStub2<P1, P2, R> stub, P1 p1, P2 p2);

    /*
    ... probably up to 5?
     */
}
