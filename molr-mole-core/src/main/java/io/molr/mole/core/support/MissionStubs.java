package io.molr.mole.core.support;

import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.Placeholders;
import io.molr.mole.core.support.domain.MissionStub0;
import io.molr.mole.core.support.domain.VoidStub0;

public class MissionStubs {

    public static final VoidStub0 stub(String missionName) {
        return new VoidStub0(missionName);
    }

    public static final <R> MissionStub0<R> stub(String missionName, Class<R> returnType) {
        return new MissionStub0<R>(missionName, Placeholders.returned(returnType).get());
    }
}
