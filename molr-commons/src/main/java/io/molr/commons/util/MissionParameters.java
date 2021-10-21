package io.molr.commons.util;

import com.google.common.collect.Iterables;

import io.molr.commons.domain.ListOfStrings;
import io.molr.commons.domain.MissionParameter;

public final class MissionParameters {

    /**
     * Provides a default value for the given {@link MissionParameter}
     *
     * @param param the parameter
     * @param <T> the type of the default, returned, value
     * @return the default value for the provided {@link MissionParameter}
     */
    public static <T> T defaultValueFor(MissionParameter<T> param) {
        if (param.defaultValue() != null) {
            return param.defaultValue();
        }
        if ((param.allowedValues() != null) && (!param.allowedValues().isEmpty())) {
            return Iterables.getFirst(param.allowedValues(), null);
        }
        if (param.placeholder().type().equals(String.class)) {
            return (T) "";
        }
        if (param.placeholder().type().equals(Integer.class)) {
            return (T) Integer.valueOf(0);
        }
        if (param.placeholder().type().equals(Long.class)) {
        	return (T) Long.valueOf(0);
        }
        if (param.placeholder().type().equals(Double.class)) {
            return (T) Double.valueOf(0.0);
        }
        if (param.placeholder().type().equals(Boolean.class)) {
            return (T) Boolean.valueOf(false);
        }
        if(param.placeholder().type().equals(ListOfStrings.class)) {
        	return (T) new ListOfStrings();
        }
        return null;
    }

    private MissionParameters() {
        /* only static ! */
    }

}
