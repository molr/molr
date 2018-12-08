package io.molr.mole.core.tree.exception;

import static org.slf4j.helpers.MessageFormatter.arrayFormat;

public class StrandExecutorException extends RuntimeException {

    public StrandExecutorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates the exception with the same formatting options of the slf4j error method
     */
    public StrandExecutorException(String message, Object... args) {
        super(arrayFormat(message, args).getMessage(), arrayFormat(message, args).getThrowable());
    }

}
