package io.molr.mole.core.support.domain;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

/**
 * Represents a {@link Mission} which accepts two parameters and does not return a value
 *
 * @param <P1> the type of parameter 1
 * @param <P2> the type of parameter 2
 * @author HimanshuSahu31
 */
public class VoidStub2<P1, P2> extends MissionStub2<P1, P2, Void> {

    /**
     * @param mission the {@link Mission} being represented
     * @param p1      the {@link Placeholder} for parameter 1
     * @param p2      the {@link Placeholder} for parameter 2
     */
    VoidStub2(Mission mission, Placeholder<P1> p1, Placeholder<P2> p2) {
        super(mission, Void.class, p1, p2);
    }

    /**
     * Constructs a representation of {@link Mission} which accepts two parameters and returns a value
     *
     * @param returnType the type for return value of {@link Mission} being represented
     * @param <R>        the return type
     * @return an instance of {@link MissionStub2}
     */
    public <R> MissionStub2<P1, P2, R> returning(Class<R> returnType) {
        return new MissionStub2<P1, P2, R>(mission(), returnType,
                (Placeholder<P1>) parameterAt(0), (Placeholder<P2>) parameterAt(1));
    }
}
