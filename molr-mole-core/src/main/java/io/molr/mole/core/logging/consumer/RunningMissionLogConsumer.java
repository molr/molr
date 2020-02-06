package io.molr.mole.core.logging.consumer;

import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionLog;
import io.molr.mole.core.logging.stream.LogStream;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

// TODO Add javadoc
public class RunningMissionLogConsumer implements LogConsumer<MissionLog, MissionHandle> {

    Map<MissionHandle, LogStream<MissionLog>> missionToStream = new ConcurrentHashMap<>();

    @Override
    public void update(Observable o, Object arg) {
        MissionLog missionLog = (MissionLog) arg;
        missionToStream.computeIfAbsent(missionLog.missionHandle(), m -> new LogStream<>());
        missionToStream.get(missionLog.missionHandle()).publish(missionLog);
    }

    @Override
    public Flux<MissionLog> asStream(MissionHandle handle) {
        return missionToStream.containsKey(handle) ? missionToStream.get(handle).asStream() : null;
    }
}
