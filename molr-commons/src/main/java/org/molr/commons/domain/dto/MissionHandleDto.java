package org.molr.commons.domain.dto;

import org.molr.commons.domain.MissionHandle;

public class MissionHandleDto {

    public final String id;

    public MissionHandleDto(String id) {
        this.id = id;
    }

    public MissionHandleDto() {
        this.id = null;
    }

    public static final MissionHandleDto from(MissionHandle handle) {
        return new MissionHandleDto(handle.id());
    }

    public MissionHandle toMissionHandle() {
        return MissionHandle.ofId(this.id);
    }

    @Override
    public String toString() {
        return "MissionHandleDto{" +
                "id='" + id + '\'' +
                '}';
    }
}
