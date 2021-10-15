package io.molr.mole.core.support.domain;

import java.util.Map;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

/**
 * Represents a {@link Mission} which accepts one parameter and returns a value
 *
 * @param <P1> the type for parameter 1
 * @param <R>  the type for return value of {@link Mission} being represented
 * @author HimanshuSahu31
 */
public class MissionStub1<P1, R> {

    private StubData data;

    /**
     * @param mission    the {@link Mission} being represented
     * @param returnType the type for return value of {@link Mission} being represented
     */
    MissionStub1(Mission mission, Class<R> returnType, Placeholder<P1> p1) {
        data = StubData.from(mission)
                .setReturnType(returnType)
                .addParameter(p1)
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
     * @return the parameter map constructed with the arguments provided
     */
    public Map<String, Object> parameters(P1 p1) {
        return data.parameters(p1);
    }

    /**
     * @param index the index of parameter
     * @return the {@link Placeholder} for parameter at given index
     */
    protected Placeholder<?> parameterAt(int index) {
        return data.parameterAt(index);
    }
}
