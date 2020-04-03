package io.molr.mole.core.logging;

import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionLog;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO Add javadoc
public class MissionLogHandler {
    private static Map<MissionHandle, MissionLogger> missionToLogs = new ConcurrentHashMap<>();

    public static void init(MissionHandle handle) {
        if (!missionToLogs.containsKey(handle)) {
            missionToLogs.put(handle, new MissionLogger(handle));
        }
    }

    public static void publish(MissionHandle handle, MissionLog log) {
        missionToLogs.get(handle).publish(log);
    }

    public static Flux<MissionLog> asStream(MissionHandle handle) {
        return missionToLogs.get(handle).asStream();
    }

    public static MissionLogger get(MissionHandle handle) {
        return missionToLogs.get(handle);
    }
}
