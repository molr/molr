package io.molr.mole.core.tree.exception;

import static org.slf4j.helpers.MessageFormatter.arrayFormat;

public class StrandExecutorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public StrandExecutorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates the exception with the same formatting options of the slf4j error method
     * 
     * @param message the message of the exception, which can contain "{}" - type placeholders
     * @param args the arguments for filling the placeholders of the message
     */
    public StrandExecutorException(String message, Object... args) {
        super(arrayFormat(message, args).getMessage(), arrayFormat(message, args).getThrowable());
    }

}
