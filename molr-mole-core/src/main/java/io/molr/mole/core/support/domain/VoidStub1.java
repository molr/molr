package io.molr.mole.core.support.domain;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

/**
 * Represents a {@link Mission} which accepts one parameter and does not return a value
 *
 * @param <P1> the type of parameter 1
 * @author HimanshuSahu31
 */
public class VoidStub1<P1> extends MissionStub1<P1, Void> {

    /**
     * @param mission the {@link Mission} being represented
     * @param p1 the {@link Placeholder} for parameter 1
     */
    VoidStub1(Mission mission, Placeholder<P1> p1) {
        super(mission, Void.class, p1);
    }

    /**
     * Constructs a representation of {@link Mission} which accepts one parameter and returns a value
     *
     * @param returnType the type for return value of {@link Mission} being represented
     * @param <R> the return type
     * @return an instance of {@link MissionStub1}
     */
    public <R> MissionStub1<P1, R> returning(Class<R> returnType) {
        return new MissionStub1<>(mission(), returnType, (Placeholder<P1>) parameterAt(0));
    }
}
