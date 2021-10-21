package io.molr.mole.core.tree.exception;

/**
 * Exception that indicates that the command was rejected
 */
public class RejectedCommandException extends StrandExecutorException {

    private static final long serialVersionUID = 1L;

    public RejectedCommandException(String message, Object... args) {
        super(message, args);
    }

}
