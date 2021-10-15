package io.molr.mole.core.support.domain;

import java.util.Map;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

/**
 * Represents a {@link Mission} which accepts thre parameters and returns a value
 *
 * @param <P1> the type for parameter 1
 * @param <P2> the type for parameter 2
 * @param <P3> the type for parameter 3
 * @param <R>  the type for return value of {@link Mission} being represented
 * @author HimanshuSahu31
 */
public class MissionStub3<P1, P2, P3, R> {

    private StubData data;

    MissionStub3(Mission mission, Class<R> returnType, Placeholder<P1> p1, Placeholder<P2> p2, Placeholder<P3> p3) {
        data = StubData.from(mission)
                .setReturnType(returnType)
                .addParameter(p1)
                .addParameter(p2)
                .addParameter(p3)
                .build();
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
     * @param p1 the parameter 1
     * @param p2 the parameter 2
     * @return the parameter map constructed with the arguments provided
     */
    public Map<String, Object> parameters(P1 p1, P2 p2, P3 p3) {
        return data.parameters(p1, p2, p3);
    }

    /**
     * @param index the index of parameter
     * @return the {@link Placeholder} for parameter at given index
     */
    protected Placeholder<?> parameterAt(int index) {
        return data.parameterAt(index);
    }
}
