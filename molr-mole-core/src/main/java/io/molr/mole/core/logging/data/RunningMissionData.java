package io.molr.mole.core.logging.data;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.molr.commons.domain.MissionLog;

import java.util.Map;

// TODO Add Javadoc
public class RunningMissionData implements LogData {
    @Override
    public Object extract(ILoggingEvent event) {
        Map<String, String> mdc = event.getMDCPropertyMap();
        return MissionLog.from(mdc.get("missionHandle")).time(event.getTimeStamp()).strand(mdc.get("strand"))
                .block(mdc.get("block")).message(event.getFormattedMessage()).build();
    }
}
