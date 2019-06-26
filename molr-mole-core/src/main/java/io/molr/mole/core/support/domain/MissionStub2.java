package io.molr.mole.core.support.domain;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

import java.util.Map;

public class MissionStub2<P1, P2, R> {

    protected StubData data;

    MissionStub2(Mission mission, Placeholder<R> returnType, Placeholder<P1> p1, Placeholder<P2> p2) {
        data = StubData.from(mission)
                .setReturnType(returnType)
                .addParameter(p1)
                .addParameter(p2);
    }

    public Mission getMission() {
        return data.getMission();
    }

    public Placeholder<R> getReturnType() {
        return (Placeholder<R>) data.getReturnType();
    }

    public Map<String, Object> getParametersMap(P1 p1, P2 p2) {
        return data.getParametersMap(p1, p2);
    }
}
