package io.molr.mole.core.support.domain;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

import java.util.Map;

/**
 * Represents a {@link Mission} which accepts two parameters and returns a value
 *
 * @param <P1> the type for parameter 1
 * @param <P2> the type for parameter 2
 * @param <R>  the type for return value of {@link Mission} being represented
 * @author HimanshuSahu31
 */
public class MissionStub2<P1, P2, R> {

    private StubData data;

    /**
     * @param mission
     * @param returnType the type for return value of {@link Mission} being represented
     * @param p1         the {@link Placeholder} for parameter 1
     * @param p2         the {@link Placeholder} for parameter 2
     */
    MissionStub2(Mission mission, Class<R> returnType, Placeholder<P1> p1, Placeholder<P2> p2) {
        data = StubData.from(mission)
                .setReturnType(returnType)
                .addParameter(p1)
                .addParameter(p2)
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
    public Map<String, Object> parameters(P1 p1, P2 p2) {
        return data.parameters(p1, p2);
    }

    /**
     * @param index the index of parameter
     * @return the {@link Placeholder} for parameter at given index
     */
    protected Placeholder<?> parameterAt(int index) {
        return data.parameterAt(index);
    }
}
