package io.molr.mole.core.logging.appender;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import io.molr.mole.core.logging.data.LogData;
import io.molr.mole.core.logging.publisher.LogPublisher;

// TODO Add javadoc
public class LogAppender extends AppenderBase<ILoggingEvent> {

    private LogData data;
    private LogPublisher publisher;

    public LogAppender(LogData data, LogPublisher publisher) {
        this.data = data;
        this.publisher = publisher;
    }

    @Override
    protected void append(ILoggingEvent event) {
        publisher.publish(data.extract(event));
    }
}
