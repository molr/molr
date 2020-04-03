package io.molr.commons.domain.dto;

import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionLog;

public class MissionLogDto {

    public final String time;
    public final String missionHandle;
    public final String strandId;
    public final String blockId;
    public final String message;

    public MissionLogDto(String time, String missionHandle, String strandId, String blockId, String message) {
        this.time = time;
        this.missionHandle = missionHandle;
        this.strandId = strandId;
        this.blockId = blockId;
        this.message = message;
    }

    public MissionLogDto() {
        this.time = null;
        this.missionHandle = null;
        this.strandId = null;
        this.blockId = null;
        this.message = null;
    }

    public static final MissionLogDto from(MissionLog missionLog) {
        return new MissionLogDto(missionLog.time(), missionLog.missionHandle(), missionLog.strandId(),
                missionLog.blockId(), missionLog.message());
    }

    public MissionLog toMissionLog() {
        return MissionLog.from(MissionHandle.ofId(missionHandle)).time(time).strand(strandId).block(blockId)
                .message(message).build();
    }

    @Override
    public String toString() {
        return "MissionLogDto{" +
                "time='" + time + '\'' +
                ", missionHandle='" + missionHandle + '\'' +
                ", strandId='" + strandId + '\'' +
                ", blockId='" + blockId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
