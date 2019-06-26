package io.molr.mole.core.support.domain;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

import static io.molr.commons.domain.Placeholders.returned;

public class VoidStub1<P1> extends MissionStub1<P1, Void> {

    VoidStub1(Mission mission, Placeholder<P1> p1) {
        super(mission, Placeholder.aVoid("voidReturnValue"), p1);
    }

    public <R> MissionStub1<P1, R> returning(Class<R> returnType) {
        return new MissionStub1<P1, R>(data.getMission(), returned(returnType).get(),
                (Placeholder<P1>) data.getParameter(0));
    }
}
