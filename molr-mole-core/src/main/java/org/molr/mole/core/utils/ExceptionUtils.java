package org.molr.mole.core.utils;

import org.slf4j.helpers.FormattingTuple;

import java.lang.reflect.Constructor;

import static org.slf4j.helpers.MessageFormatter.arrayFormat;

public class ExceptionUtils {

    /**
     * Instantiate the provided exception {@link Class}. It will look for a constructor compatible with {@link String}
     * and {@link Throwable}. In case such constructor does not exists, this method will throw an {@link IllegalArgumentException}
     */
    public static <T extends Exception> T exception(Class<T> clazz, String message, Object... args) {
        FormattingTuple formatter = arrayFormat(message, args);
        try {
            Constructor<T> constructor = clazz.getConstructor(String.class, Throwable.class);
            return constructor.newInstance(formatter.getMessage(), formatter.getThrowable());
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot instantiate exception class " + clazz.getSimpleName());
        }
    }

}
