package io.molr.mole.core.support.domain;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

import static io.molr.commons.domain.Placeholders.returned;

public class VoidStub0 extends MissionStub0<Void> {

    public VoidStub0(Mission mission) {
        super(mission, Placeholder.aVoid("voidReturnValue"));
    }

    public VoidStub0(String missionName) {
        this(new Mission(missionName));
    }

    public <R> MissionStub0<R> returning(Class<R> returnType) {
        return new MissionStub0<R>(data.getMission(), returned(returnType).get());
    }

    public <P1> VoidStub1<P1> withParameters(Placeholder<P1> p1) {
        return new VoidStub1<P1>(getMission(), p1);
    }

    public <P1, P2> VoidStub2<P1, P2> withParameters(Placeholder<P1> p1, Placeholder<P2> p2) {
        return new VoidStub2<P1, P2>(getMission(), p1, p2);
    }
}
