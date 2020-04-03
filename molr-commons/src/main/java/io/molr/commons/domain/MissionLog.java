package io.molr.commons.domain;

import io.molr.commons.domain.dto.MissionLogDto;

import java.text.MessageFormat;
import java.time.LocalDateTime;

public final class MissionLog {
    private final String time;
    private final String missionHandle;
    private final String strandId;
    private final String blockId;
    private final String message;

    private MissionLog(Builder builder) {
        this.time = builder.time;
        this.missionHandle = builder.handleId;
        this.strandId = builder.strandId;
        this.blockId = builder.blockId;
        this.message = builder.message;
    }

    public static Builder from(MissionHandle handle) {
        return new Builder(handle);
    }

    public String time() {
        return time;
    }

    public String missionHandle() {
        return missionHandle;
    }

    public String strandId() {
        return strandId;
    }

    public String blockId() {
        return blockId;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return MissionLogDto.from(this).toString();
    }

    public static class Builder {

        private String handleId;
        private String time;
        private String strandId;
        private String blockId;
        private String message;

        private Builder(MissionHandle handle) {
            this.handleId = handle.id();
            this.time = LocalDateTime.now().toString();
        }

        public Builder time(String time) {
            this.time = time;
            return this;
        }

        public Builder strand(Strand strand) {
            this.strandId = strand.id();
            return this;
        }

        public Builder strand(String strandId) {
            this.strandId = strandId;
            return this;
        }

        public Builder block(Block block) {
            this.blockId = block.id();
            return this;
        }

        public Builder block(String blockId) {
            this.blockId = blockId;
            return this;
        }

        public Builder message(Exception e, String template, Object... params) {
            this.message = formatMessage(template, params) + "/n" + e.getStackTrace().toString();
            return this;
        }

        public Builder message(String template, Object... params) {
            this.message = formatMessage(template, params);
            return this;
        }

        private String formatMessage(String template, Object... params) {
            if (params.length > 0) {
                return MessageFormat.format(template, params);
            }
            return template;
        }

        public MissionLog build() {
            return new MissionLog(this);
        }
    }
}
