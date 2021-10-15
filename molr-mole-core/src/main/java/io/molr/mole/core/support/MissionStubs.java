package io.molr.mole.core.support;

import io.molr.commons.domain.Mission;
import io.molr.mole.core.support.domain.MissionStub0;
import io.molr.mole.core.support.domain.VoidStub0;

/**
 * A factory class to generate Mission Stubs
 *
 * @author HimanshuSahu31
 */
public final class MissionStubs {

    /**
     * Constructs a {@link VoidStub0} which has zero arguments and no return type
     *
     * @param missionName the name of the {@link Mission}
     * @return an instance of {@link VoidStub0}
     */
    public static VoidStub0 stub(String missionName) {
        return new VoidStub0(missionName);
    }

    /**
     * Constructs a {@link MissionStub0} which has zero arguments and a return type
     *
     * @param missionName the name of the {@link Mission}
     * @param returnType the return type of the {@link Mission}
     * @param <R> the type for return value of the {@link Mission}
     * @return an instance of {@link MissionStub0}
     */
    public static <R> MissionStub0<R> stub(String missionName, Class<R> returnType) {
        return new MissionStub0<>(missionName, returnType);
    }
}
