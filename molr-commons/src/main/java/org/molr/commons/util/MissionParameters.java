package org.molr.commons.util;

import org.molr.commons.domain.MissionParameter;

public final class MissionParameters {

    /**
     * Provides a default value for the given {@link MissionParameter}
     *
     * @param param the parameter
     * @param <T>   the type of the default, returned, value
     * @return the default value for the provided {@link MissionParameter}
     */
    public static <T> T defaultValueFor(MissionParameter<T> param) {
        if (param.defaultValue() != null) {
            return param.defaultValue();
        }
        if (param.placeholder().type().equals(String.class)) {
            return (T) "";
        }
        if (param.placeholder().type().equals(Integer.class)) {
            return (T) new Integer(0);
        }
        if (param.placeholder().type().equals(Double.class)) {
            return (T) new Double(0.0);
        }
        if (param.placeholder().type().equals(Boolean.class)) {
            return (T) new Boolean(false);
        }
        return null;
    }

    private MissionParameters() {
        /* only static ! */
    }

}
