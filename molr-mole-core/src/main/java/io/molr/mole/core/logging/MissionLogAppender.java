package io.molr.mole.core.logging;

import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionLog;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

// TODO Add javadoc
public class MissionLogAppender extends AppenderSkeleton {

    public MissionLogAppender(MissionHandle handle) {
        addFilter(new MissionHandleFilter(handle.id()));
    }

    @Override
    protected void append(LoggingEvent event) {
        if (headFilter.decide(event) == Filter.ACCEPT) {
            MissionLog missionLog = (MissionLog) event.getMessage();
            MissionLogHandler.publish(MissionHandle.ofId(missionLog.missionHandle()), missionLog);
        }
    }

    @Override
    public void close() {
        clearFilters();
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    private static class MissionHandleFilter extends Filter {

        private final String handleId;

        public MissionHandleFilter(String handleId) {
            this.handleId = handleId;
        }

        @Override
        public int decide(LoggingEvent event) {
            MissionLog missionLog = (MissionLog) event.getMessage();
            if (handleId.equals(missionLog.missionHandle())) {
                return ACCEPT;
            } else {
                return DENY;
            }
        }
    }
}
