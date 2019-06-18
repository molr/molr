package io.molr.mole.core.support.domain;

public interface VoidStub2<P1, P2> extends MissionStub2<P1, P2, Void> {

    <R> MissionStub2<P1, P2, R> returning(Class<R> returnType);

}
