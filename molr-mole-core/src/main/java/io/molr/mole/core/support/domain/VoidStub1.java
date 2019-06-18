package io.molr.mole.core.support.domain;

public interface VoidStub1<P1> extends MissionStub1<P1, Void> {

    <R> MissionStub1<P1, R> returning(Class<R> returnType);
}
