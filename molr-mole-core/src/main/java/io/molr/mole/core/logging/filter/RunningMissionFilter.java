package io.molr.mole.core.logging.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.springframework.util.StringUtils;

// TODO Add javadoc
public class RunningMissionFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        return StringUtils.isEmpty(event.getMDCPropertyMap().get("missionHandle")) ? FilterReply.DENY : FilterReply.ACCEPT;
    }
}
