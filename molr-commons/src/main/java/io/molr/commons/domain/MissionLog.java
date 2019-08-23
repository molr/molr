package io.molr.commons.domain;

import io.molr.commons.domain.dto.MissionLogDto;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class MissionLog {
    private final Date time;
    private final MissionHandle missionHandle;
    private final Strand strand;
    private final String block;
    private final String message;

    public final static DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");

    private MissionLog(Builder builder) {
        this.time = builder.time;
        this.missionHandle = builder.missionHandle;
        this.strand = builder.strand;
        this.block = builder.block;
        this.message = builder.message;
    }

    public static Builder from(String handleId) {
        return new Builder(MissionHandle.ofId(handleId));
    }

    public Date time() {
        return time;
    }

    public MissionHandle missionHandle() {
        return missionHandle;
    }

    public Strand strand() {
        return strand;
    }

    public String block() {
        return block;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return MissionLogDto.from(this).toString();
    }

    public static class Builder {

        private Date time;
        private MissionHandle missionHandle;
        private Strand strand;
        private String block;
        private String message;

        private Builder(MissionHandle handle) {
            this.missionHandle = handle;
        }

        public Builder time(long time) {
            this.time = new Date(time);
            return this;
        }

        public Builder time(Date time) {
            this.time = time;
            return this;
        }

        public Builder strand(Strand strand) {
            this.strand = strand;
            return this;
        }

        public Builder strand(String strandId) {
            this.strand = strand == null ? null : Strand.ofId(strandId);
            return this;
        }

        public Builder block(String block) {
            this.block = block;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public MissionLog build() {
            MissionLog log = new MissionLog(this);
            return log;
        }
    }
}
