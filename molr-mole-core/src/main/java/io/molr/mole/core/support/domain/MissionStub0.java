package io.molr.mole.core.support.domain;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

import java.util.Collections;
import java.util.Map;

public class MissionStub0<R> {

    protected StubData data;

    public MissionStub0(Mission mission, Placeholder<R> returnType) {
        this.data = StubData.from(mission)
                .setReturnType(returnType);
    }

    public MissionStub0(String missionName, Placeholder<R> returnType) {
        this(new Mission(missionName), returnType);
    }

    public <P1> MissionStub1<P1, R> withParameters(Placeholder<P1> p1) {
        return new MissionStub1<P1, R>(getMission(), getReturnType(), p1);
    }

    public <P1, P2> MissionStub2<P1, P2, R> withParameters(Placeholder<P1> p1, Placeholder<P2> p2) {
        return new MissionStub2<P1, P2, R>(getMission(), getReturnType(), p1, p2);
    }

    public Mission getMission() {
        return data.getMission();
    }

    public Placeholder<R> getReturnType() {
        return (Placeholder<R>) data.getReturnType();
    }

    public Map<String, Object> getParametersMap() {
        return Collections.emptyMap();
    }
}
