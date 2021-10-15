package io.molr.mole.core.support.domain;

import java.util.Map;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

/**
 * Represents a {@link Mission} which does not accept parameters and returns a value
 *
 * @param <R> the type for return value of {@link Mission} being represented
 * @author HimanshuSahu31
 */
public class MissionStub0<R> {

    private StubData data;

    /**
     * @param mission    the {@link Mission} being represented
     * @param returnType the type for return value of {@link Mission} being represented
     */
    public MissionStub0(Mission mission, Class<R> returnType) {
        this.data = StubData.from(mission)
                .setReturnType(returnType)
                .build();
    }

    /**
     * @param missionName the mission name
     * @param returnType  the type for return value of {@link Mission} being represented
     */
    public MissionStub0(String missionName, Class<R> returnType) {
        this(new Mission(missionName), returnType);
    }

    /**
     * Constructs a representation of {@link Mission} which accepts one parameter and returns a value
     *
     * @param p1   the {@link Placeholder} for parameter 1
     * @param <P1> the type of parameter 1
     * @return an instance of {@link MissionStub1}
     */
    public <P1> MissionStub1<P1, R> withParameters(Placeholder<P1> p1) {
        return new MissionStub1<P1, R>(mission(), returnType(), p1);
    }

    /**
     * Constructs a representation of {@link Mission} which accepts two parameters and returns a value
     *
     * @param p1   the {@link Placeholder} for parameter 1
     * @param p2   the {@link Placeholder} for parameter 2
     * @param <P1> the type of parameter 1
     * @param <P2> the type of parameter 2
     * @return an instance of {@link MissionStub1}
     */
    public <P1, P2> MissionStub2<P1, P2, R> withParameters(Placeholder<P1> p1, Placeholder<P2> p2) {
        return new MissionStub2<P1, P2, R>(mission(), returnType(), p1, p2);
    }

    /**
     * @return the {@link Mission} being represented
     */
    public Mission mission() {
        return data.mission();
    }

    /**
     * @return the type for return value of {@link Mission} being represented
     */
    public Class<R> returnType() {
        return (Class<R>) data.returnType();
    }

    /**
     * The parameter map required by the represented {@link Mission} is constructed by the arguments provided.
     *
     * @return the parameter map constructed with the arguments provided. In this case, there are zero args
     */
    public Map<String, Object> parameters() {
        return data.parameters();
    }
}
