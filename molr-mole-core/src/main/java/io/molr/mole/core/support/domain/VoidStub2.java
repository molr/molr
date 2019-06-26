package io.molr.mole.core.support.domain;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

import static io.molr.commons.domain.Placeholders.returned;

public class VoidStub2<P1, P2> extends MissionStub2<P1, P2, Void> {

    VoidStub2(Mission mission, Placeholder<P1> p1, Placeholder<P2> p2) {
        super(mission, Placeholder.aVoid("voidReturnValue"), p1, p2);
    }

    public <R> MissionStub2<P1, P2, R> returning(Class<R> returnType) {
        return new MissionStub2<P1, P2, R>(data.getMission(), returned(returnType).get(),
                (Placeholder<P1>) data.getParameter(0), (Placeholder<P2>) data.getParameter(1));
    }
}
