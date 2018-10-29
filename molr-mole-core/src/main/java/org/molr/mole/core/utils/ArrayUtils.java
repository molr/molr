package org.molr.mole.core.utils;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class ArrayUtils {

    /**
     * TODO Introduce apache commons dependency??
     */
    public static <T> List<T> convertArrayTo(Object[] array, Class<T> clazz) {
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        for (Object obj : array) {
            builder.add(clazz.cast(obj));
        }
        return builder.build();
    }
}
