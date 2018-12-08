package io.molr.commons.util;

import org.slf4j.helpers.FormattingTuple;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;

import static org.slf4j.helpers.MessageFormatter.arrayFormat;

public class Exceptions {

    /**
     * Instantiate the provided exception {@link Class}. It will look for a constructor compatible with {@link String}
     * and {@link Throwable}. In case such constructor does not exists, this method will throw an {@link
     * IllegalArgumentException}
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

    public static IllegalStateException illegalStateException(String message, Object... args) {
        return exception(IllegalStateException.class, message, args);
    }

    public static IllegalArgumentException illegalArgumentException(String message, Object... args) {
        return exception(IllegalArgumentException.class, message, args);
    }

    public static String stackTraceFrom(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}
