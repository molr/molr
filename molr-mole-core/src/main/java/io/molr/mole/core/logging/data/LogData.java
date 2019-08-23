package io.molr.mole.core.logging.data;

import ch.qos.logback.classic.spi.ILoggingEvent;

// TODO Add javadoc
public interface LogData {
    Object extract(ILoggingEvent event);
}
