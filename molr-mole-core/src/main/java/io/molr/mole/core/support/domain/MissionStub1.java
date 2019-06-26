package io.molr.mole.core.support.domain;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

import java.util.Map;

public class MissionStub1<P1, R> {

    protected StubData data;

    MissionStub1(Mission mission, Placeholder<R> returnType, Placeholder<P1> p1) {
        data = StubData.from(mission)
                .setReturnType(returnType)
                .addParameter(p1);
    }

    public Mission getMission() {
        return data.getMission();
    }

    public Placeholder<R> getReturnType() {
        return (Placeholder<R>) data.getReturnType();
    }

    public Map<String, Object> getParametersMap(P1 p1) {
        return data.getParametersMap(p1);
    }
}
