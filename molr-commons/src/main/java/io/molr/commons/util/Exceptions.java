package io.molr.commons.util;

import static org.slf4j.helpers.MessageFormatter.arrayFormat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;

import org.slf4j.helpers.FormattingTuple;

public class Exceptions {

    /**
     * Instantiate the provided exception {@link Class}. It will look for a constructor compatible with {@link String}
     * and {@link Throwable}. In case such constructor does not exists, this method will throw an {@link
     * IllegalArgumentException}. The message can contain "{}" type placeholders, like with common logging APIs.
     * 
     * @param exceptionClass the type of the exception to instantiate
     * @param message the message (which can contain "{}" type of placeholders)
     * @param args the arguments to the message, which will replace the "{}" type placeholders. In case of a throwable,
     *            it is passed as the second argumet of the exception constructor.
     * @param <T> the type of the exception
     * @return an instance of the exception
     * @throws IllegalArgumentException in case the exception of the given type does not have an appropriate
     *             constructor.
     */
    public static <T extends Exception> T exception(Class<T> exceptionClass, String message, Object... args) {
        FormattingTuple formatter = arrayFormat(message, args);
        try {
            Constructor<T> constructor = exceptionClass.getConstructor(String.class, Throwable.class);
            return constructor.newInstance(formatter.getMessage(), formatter.getThrowable());
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot instantiate exception class " + exceptionClass.getSimpleName());
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
