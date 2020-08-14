package io.molr.mole.core.support.domain;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

public class VoidStub3<P1, P2, P3> extends MissionStub3<P1, P2, P3, Void> {

    VoidStub3(Mission mission, Placeholder<P1> p1, Placeholder<P2> p2, Placeholder<P3> p3) {
        super(mission, Void.class, p1, p2, p3);
    }

    public <R> MissionStub3<P1, P2, P3, R> returning(Class<R> returnType) {
        return new MissionStub3<P1, P2, P3, R>(mission(), returnType,
                (Placeholder<P1>) parameterAt(0), (Placeholder<P2>) parameterAt(1), (Placeholder<P3>) parameterAt(2));
    }
}
