package io.molr.mole.core.tree.exception;

import io.molr.commons.domain.StrandCommand;

/**
 * Exception that indicates that the command was rejected
 */
public class RejectedCommandException extends StrandExecutorException {

    public RejectedCommandException(StrandCommand command, String message, Object... args) {
        super(message, args);
    }

}
