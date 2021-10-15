package io.molr.mole.core.support.domain;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.Placeholder;

/**
 * Provides capabilities to Missions Stubs for accessing data and generating parameters
 *
 * @author HimanshuSahu31
 */
public final class StubData {

    private final Mission mission;
    private final Class<?> returnType;
    private final List<Placeholder<?>> parameters;

    /**
     * @param builder the {@link Builder} to initialize fields
     */
    private StubData(Builder builder) {
        this.mission = builder.mission;
        this.returnType = builder.returnType;
        this.parameters = new ArrayList<>(builder.parameters);
    }

    /**
     * @param mission the {@link Mission} being represented
     * @return the {@link Builder} to build {@link StubData}
     */
    public static Builder from(Mission mission) {
        return new Builder(mission);
    }

    /**
     * @return the {@link Mission} being represented
     */
    public Mission mission() {
        return this.mission;
    }

    /**
     * @return the type of return value for {@link Mission} being represented
     */
    public Class<?> returnType() {
        return returnType;
    }

    /**
     * @param index the index of parameter
     * @return the {@link Placeholder} for parameter at given index
     */
    public Placeholder<?> parameterAt(int index) {
        return parameters.get(index);
    }

    /**
     * The parameter map required by the represented {@link Mission} is constructed by the arguments provided.
     *
     * @param objects the paramters to {@link Mission} being represented
     * @return the parameter map constructed with the arguments provided
     */
    public Map<String, Object> parameters(Object... objects) {
        Map<String, Object> params = new HashMap<String, Object>();
        for (int i = 0; i < objects.length; i++) {
            params.put(parameters.get(i).name(), objects[i]);
        }
        return params;
    }

    /**
     * The Builder class to build {@link StubData} with the fields set
     */
    public static class Builder {
        private final Mission mission;
        private Class<?> returnType;
        private List<Placeholder<?>> parameters = new ArrayList<Placeholder<?>>();

        /**
         * @param mission the {@link Mission} being represented
         */
        private Builder(Mission mission) {
            this.mission = requireNonNull(mission, "mission must not be null");
        }

        /**
         * @param returnType the type of return value for {@link Mission} being represented
         * @return the {@link Builder} to build {@link StubData}
         */
        public Builder setReturnType(Class<?> returnType) {
            this.returnType = requireNonNull(returnType, "return type must not be null");
            return this;
        }

        /**
         * @param param the parameter to {@link Mission} being represented
         * @return the {@link Builder} to build {@link StubData}
         */
        public Builder addParameter(Placeholder<?> param) {
            parameters.add(requireNonNull(param, "param must not be null"));
            return this;
        }

        /**
         * Builds the {@link StubData} with fields initialized by the builder
         *
         * @return an instance of {@link StubData}
         */
        public StubData build() {
            return new StubData(this);
        }
    }
}
