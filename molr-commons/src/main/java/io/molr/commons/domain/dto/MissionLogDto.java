package io.molr.commons.domain.dto;

import io.molr.commons.domain.MissionLog;

import java.text.ParseException;

public class MissionLogDto {

    public final String time;
    public final String missionHandle;
    public final String strandId;
    public final String block;
    public final String message;

    public MissionLogDto(String time, String missionHandle, String strandId, String block, String message) {
        this.time = time;
        this.missionHandle = missionHandle;
        this.strandId = strandId;
        this.block = block;
        this.message = message;
    }

    public MissionLogDto() {
        this.time = null;
        this.missionHandle = null;
        this.strandId = null;
        this.block = null;
        this.message = null;
    }

    public static final MissionLogDto from(MissionLog missionLog) {
        return new MissionLogDto(missionLog.time() == null? null : MissionLog.dateFormatter.format(missionLog.time()),
                missionLog.missionHandle().id(),
                missionLog.strand() == null ? null : missionLog.strand().id(),
                missionLog.block(), missionLog.message());
    }

    public MissionLog toMissionLog() {
        try {
            return MissionLog.from(missionHandle).time(MissionLog.dateFormatter.parse(time)).strand(strandId).block(block)
                    .message(message).build();
        } catch (ParseException e) {
            return MissionLog.from(missionHandle).strand(strandId).block(block).message(message).build();
        }
    }

    @Override
    public String toString() {
        return "MissionLogDto{" +
                "time='" + time + '\'' +
                ", missionHandle='" + missionHandle + '\'' +
                ", strandId='" + strandId + '\'' +
                ", block='" + block + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
