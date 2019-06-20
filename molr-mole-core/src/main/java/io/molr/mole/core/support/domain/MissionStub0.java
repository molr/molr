package io.molr.mole.core.support.domain;

import io.molr.commons.domain.Placeholder;

public interface MissionStub0<R> {

    <P1> MissionStub1<P1, R> withParameters(Placeholder<P1> p1);

    <P1, P2> MissionStub2<P1, P2, R> withParameters(Placeholder<P1> p1, Placeholder<P2> p2);

}
