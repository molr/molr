package io.molr.mole.core.logging;

import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionLog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

// TODO Add javadoc
public class MissionLogger {
    private ReplayProcessor<MissionLog> missionLogsSink;
    private Flux<MissionLog> missionLogsStream;

    public MissionLogger(MissionHandle handle) {
        this.missionLogsSink = ReplayProcessor.cacheLast();
        this.missionLogsStream = missionLogsSink.publishOn(Schedulers.newSingle("mission-logs-" + handle.id()));
    }

    public void publish(MissionLog msg) {
        missionLogsSink.onNext(msg);
    }

    public Flux<MissionLog> asStream() {
        return missionLogsStream;
    }

}

