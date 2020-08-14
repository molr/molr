package io.molr.commons.domain;

import com.google.common.collect.ImmutableSet;

import java.util.Optional;
import java.util.Set;

public final class Placeholders {

    private static final String RETURN_VALUE_KEY = "returnValue";
    private static final String THROWABLE_KEY = "throwable";
    private static final String CONTEXT_KEY = "context";

    public static final Placeholder<String> RETURNED_STRING = Placeholder.aString(RETURN_VALUE_KEY);
    public static final Placeholder<Integer> RETURNED_INTEGER = Placeholder.anInteger(RETURN_VALUE_KEY);
    public static final Placeholder<Double> RETURNED_DOUBLE = Placeholder.aDouble(RETURN_VALUE_KEY);
    public static final Placeholder<Boolean> RETURNED_BOOLEAN = Placeholder.aBoolean(RETURN_VALUE_KEY);

    private static final Placeholder<Object> CONTEXT = Placeholder.of(Object.class, CONTEXT_KEY);

    private static final Set<Placeholder<?>> ALL_RETURN_VALUES = ImmutableSet.of(RETURNED_STRING, RETURNED_INTEGER,
            RETURNED_DOUBLE, RETURNED_BOOLEAN);

    public static final Placeholder<String> THROWN = Placeholder.aString(THROWABLE_KEY);

    private Placeholders() {
        /* only constants and methods */
    }

    public static <T> Optional<Placeholder<T>> returned(Class<T> returnType) {
        return ALL_RETURN_VALUES.stream().filter(p -> p.type().isAssignableFrom(returnType)).map(p -> (Placeholder<T>) p).findAny();
    }

    public static <T> Placeholder<T> context() {
        return (Placeholder<T>) CONTEXT;
    }


}
