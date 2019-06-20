package io.molr.mole.core.support.domain;

import io.molr.commons.domain.Placeholder;

public interface VoidStub0 extends MissionStub0<Void> {

    <R> MissionStub0<R> returning(Class<R> returnType);

    <P1> VoidStub1<P1> withParameters(Placeholder<P1> p1);

    <P1, P2> VoidStub2<P1, P2> withParameters(Placeholder<P1> p1, Placeholder<P2> p2);


}
