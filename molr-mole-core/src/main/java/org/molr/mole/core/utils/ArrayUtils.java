package org.molr.mole.core.utils;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class ArrayUtils {

    /**
     * Cast each object of the array to the specified class. Incompatible types will throw {@link ClassCastException}.
     * <p>
     * TODO use apache commons?
     */
    public static <T> List<T> convertArrayTo(Object[] array, Class<T> clazz) {
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        for (Object obj : array) {
            builder.add(clazz.cast(obj));
        }
        return builder.build();
    }
}
