package io.molr.mole.core.support.domain;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

/**
 * Represents a {@link Mission} which does not accept any parameter and does not return a value
 *
 * @author HimanshuSahu31
 */
public class VoidStub0 extends MissionStub0<Void> {

    /**
     * @param mission the {@link Mission} being represented
     */
    public VoidStub0(Mission mission) {
        super(mission, Void.class);
    }

    /**
     * @param missionName the mission name
     */
    public VoidStub0(String missionName) {
        this(new Mission(missionName));
    }

    /**
     * Constructs a representation of {@link Mission} which does not accept any parameter and returns a value
     *
     * @param returnType the type for return value of {@link Mission} being represented
     * @param <R>        the return type
     * @return an instance of {@link MissionStub0}
     */
    public <R> MissionStub0<R> returning(Class<R> returnType) {
        return new MissionStub0<R>(mission(), returnType);
    }

    /**
     * Constructs a representation of {@link Mission} which accepts one parameter and does not return any value
     *
     * @param p1   the {@link Placeholder} for parameter 1
     * @param <P1> the type of parameter 1
     * @return an instance of {@link VoidStub1}
     */
    @Override
    public <P1> VoidStub1<P1> withParameters(Placeholder<P1> p1) {
        return new VoidStub1<P1>(mission(), p1);
    }

    /**
     * Constructs a representation of {@link Mission} which accepts two parameters and does not return any value
     *
     * @param p1   the {@link Placeholder} for parameter 1
     * @param p2   the {@link Placeholder} for parameter 2
     * @param <P1> the type of parameter 1
     * @param <P2> the type of parameter 1
     * @return an instance of {@link VoidStub2}
     */
    public <P1, P2> VoidStub2<P1, P2> withParameters(Placeholder<P1> p1, Placeholder<P2> p2) {
        return new VoidStub2<P1, P2>(mission(), p1, p2);
    }
}
